/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow.invoker;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fel.tool.service.ToolExecuteService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

/**
 * 针对 {@link  StoreToolInvoker} 的测试
 *
 * @author songyongtan
 * @since 2025/1/11
 */
@ExtendWith(MockitoExtension.class)
class StoreToolInvokerTest {
    @Mock
    private ToolExecuteService toolExecuteService;

    private StoreToolInvoker storeToolInvoker;

    @BeforeEach
    void setUp() {
        this.storeToolInvoker = new StoreToolInvoker(this.toolExecuteService);
    }

    @Test
    void shouldReturnTrueWhenMatchGivenNoAppTag() {
        ToolData appToolData = new ToolData();
        appToolData.setRunnables(Collections.singletonMap("BUILTIN", new Object()));

        Assertions.assertTrue(this.storeToolInvoker.match(appToolData));
    }

    @Test
    void shouldReturnFalseWhenMatchGivenHasAppTag() {
        ToolData appToolData = new ToolData();
        appToolData.setRunnables(Collections.singletonMap("APP", new Object()));

        Assertions.assertFalse(this.storeToolInvoker.match(appToolData));
    }

    @Test
    void shouldCallToolExecuteServiceWhenInvoke() {
        String arguments = "{\"inputParams\":{}";
        ToolCall tooCall = ToolCall.custom().name("toolName").arguments(arguments).build();
        String expectResult = "result";
        Mockito.when(this.toolExecuteService.execute(tooCall.name(), arguments)).thenReturn(expectResult);

        String invokeResult = this.storeToolInvoker.invoke(tooCall, null);

        Assertions.assertEquals(expectResult, invokeResult);
    }
}