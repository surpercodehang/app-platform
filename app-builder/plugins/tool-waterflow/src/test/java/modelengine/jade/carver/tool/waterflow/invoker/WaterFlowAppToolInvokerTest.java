/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow.invoker;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fit.jober.aipp.constants.AippConst;
import modelengine.fit.jober.common.FlowDataConstant;
import modelengine.fit.waterflow.entity.FlowErrorInfo;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对 {@link WaterFlowAppToolInvoker} 的测试
 *
 * @author songyongtan
 * @since 2025/1/11
 */
@ExtendWith(MockitoExtension.class)
class WaterFlowAppToolInvokerTest {
    @Mock
    private ObjectSerializer objectSerializer;

    @Mock
    private ToolInvoker toolInvoker;

    private WaterFlowAppToolInvoker waterFlowAppToolInvoker;

    @BeforeEach
    void setUp() {
        this.waterFlowAppToolInvoker = new WaterFlowAppToolInvoker(this.toolInvoker, this.objectSerializer, 1);
    }

    @Test
    void shouldReturnTrueWhenMatchGivenHasAppTag() {
        ToolData appToolData = new ToolData();
        appToolData.setRunnables(Collections.singletonMap("APP", new Object()));

        Assertions.assertTrue(this.waterFlowAppToolInvoker.match(appToolData));
    }

    @Test
    void shouldReturnFalseWhenMatchGivenNoAppTag() {
        ToolData appToolData = new ToolData();
        appToolData.setRunnables(Collections.singletonMap("BUILTIN", new Object()));

        Assertions.assertFalse(this.waterFlowAppToolInvoker.match(appToolData));
    }

    @Test
    void shouldGetResultWhenInvokeGivenCallbackResponse() {
        String expectResult = "result";
        ToolCall toolCall = ToolCall.custom().id("toolCallId1").arguments("{\"inputParams\":{}").build();
        HashMap<String, Object> toolContext = new HashMap<>();
        Mockito.when(this.toolInvoker.invoke(Mockito.argThat(arg -> arg.id().equals(toolCall.id())),
                Mockito.same(toolContext))).thenAnswer(invocation -> {
            String endNode = "endNode";
            Map<String, Object> flowData = buildFlowData(toolCall, endNode, expectResult);
            this.waterFlowAppToolInvoker.callback(Collections.singletonList(flowData));
            return "subInstanceId";
        });
        Mockito.when(this.objectSerializer.deserialize(Mockito.anyString(), Mockito.any()))
                .thenReturn(MapBuilder.get().put("inputParams", new HashMap<>()).build());
        Mockito.when(this.objectSerializer.serialize(Mockito.anyMap())).thenReturn("");
        Mockito.when(this.objectSerializer.serialize(expectResult)).thenReturn(expectResult);

        String result = this.waterFlowAppToolInvoker.invoke(toolCall, toolContext);

        Assertions.assertEquals(expectResult, result);
    }

    @Test
    void shouldThrowExceptionWhenInvokeGivenExceptionResponse() {
        String expectResult = "result";
        ToolCall tooCall = ToolCall.custom().id("toolCallId1").arguments("{\"inputParams\":{}").build();
        HashMap<String, Object> toolContext = new HashMap<>();
        Mockito.when(this.toolInvoker.invoke(Mockito.argThat(arg -> arg.id().equals(tooCall.id())),
                Mockito.same(toolContext))).thenAnswer(invocation -> {
            String nodeId = "nodeId";
            Map<String, Object> flowData = buildFlowData(tooCall, nodeId, expectResult);
            FlowErrorInfo errorInfo = new FlowErrorInfo();
            errorInfo.setErrorCode(1111);
            errorInfo.setErrorMessage("test error");
            this.waterFlowAppToolInvoker.handleException(nodeId, Collections.singletonList(flowData), errorInfo);
            return "subInstanceId";
        });
        Mockito.when(this.objectSerializer.deserialize(Mockito.anyString(), Mockito.any()))
                .thenReturn(MapBuilder.get().put("inputParams", new HashMap<>()).build());
        Mockito.when(this.objectSerializer.serialize(Mockito.anyMap())).thenReturn("");

        Assertions.assertThrows(FitException.class, () -> this.waterFlowAppToolInvoker.invoke(tooCall, toolContext));
    }

    @Test
    void shouldThrowExceptionWhenInvokeGivenNoResponse() {
        ToolCall tooCall = ToolCall.custom().id("toolCallId1").arguments("{\"inputParams\":{}").build();
        HashMap<String, Object> toolContext = new HashMap<>();
        Mockito.when(this.toolInvoker.invoke(Mockito.argThat(arg -> arg.id().equals(tooCall.id())),
                Mockito.same(toolContext))).thenAnswer(invocation -> {
            String endNode = "endNode";
            Map<String, Object> flowData = buildFlowData(tooCall, endNode, null);
            this.waterFlowAppToolInvoker.callback(Collections.singletonList(flowData));
            return "subInstanceId";
        });
        Mockito.when(this.objectSerializer.deserialize(Mockito.anyString(), Mockito.any()))
                .thenReturn(MapBuilder.get().put("inputParams", new HashMap<>()).build());
        Mockito.when(this.objectSerializer.serialize(Mockito.anyMap())).thenReturn("");

        Assertions.assertThrows(FitException.class, () -> this.waterFlowAppToolInvoker.invoke(tooCall, toolContext));
    }

    private static Map<String, Object> buildFlowData(ToolCall tooCall, String endNode, String expectResult) {
        Map<String, Object> flowData = new HashMap<>();
        List<Map<Object, Object>> input = null;
        if (expectResult != null) {
            input = Collections.singletonList(MapBuilder.get().put("input", expectResult).build());
        }
        flowData.put(AippConst.BS_DATA_KEY,
                MapBuilder.get()
                        .put("toolCallId", tooCall.id())
                        .put(FlowDataConstant.BUSINESS_DATA_INTERNAL_KEY,
                                MapBuilder.get()
                                        .put(FlowDataConstant.INTERNAL_EXECUTE_INFO_KEY,
                                                MapBuilder.get().put(endNode, input).build())
                                        .build())
                        .build());
        flowData.put(AippConst.CONTEXT_DATA_KEY, MapBuilder.get().put(FlowDataConstant.FLOW_NODE_ID, endNode).build());
        return flowData;
    }
}