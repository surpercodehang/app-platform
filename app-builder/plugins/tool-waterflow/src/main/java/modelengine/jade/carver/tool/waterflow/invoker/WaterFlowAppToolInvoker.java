/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow.invoker;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fit.jober.aipp.constants.AippConst;
import modelengine.fit.jober.common.ErrorCodes;
import modelengine.fit.jober.common.FlowDataConstant;
import modelengine.fit.jober.util.FlowDataUtils;
import modelengine.fit.waterflow.entity.FlowErrorInfo;
import modelengine.fit.waterflow.spi.FlowCallbackService;
import modelengine.fit.waterflow.spi.FlowExceptionService;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.exception.TimeoutException;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.TypeUtils;
import modelengine.jade.carver.tool.waterflow.DefaultValueFilterToolInfo;
import modelengine.jade.carver.tool.waterflow.WaterFlowToolConst;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 注册到store里的应用/工作流的执行器，由于是异步执行，这里完成异步转同步的处理，屏蔽大模型调用工具的差异
 * 注意：当前目标大模型调用应用/工作流作为工具时，不支持人工任务。如果有，则默认最大5分钟没有处理人工任务时，会出现大模型节点调用失败。
 *
 * @author songyongtan
 * @since 2024/12/25
 */
@Component("waterFlowAppToolInvoker")
public class WaterFlowAppToolInvoker extends ToolInvokerDecorator implements FlowCallbackService, FlowExceptionService {
    private static final Logger LOGGER = Logger.get(WaterFlowAppToolInvoker.class);

    private static final String INVOKER_CALLBACK_FITABLE_ID = "modelengine.jade.carver.tool.waterflow.invoker";

    private static final String TOOL_CALL_ID = "toolCallId";

    private static final long DEFAULT_TIMEOUT_SECONDS = 300L;

    private final ObjectSerializer objectSerializer;

    private final long timeout;

    /**
     * 记录调用未完成的请求。其中key为请求id, 最好使用带最大超时时间就删除的
     */
    private final Map<String, Request> requests = new ConcurrentHashMap<>();

    /**
     * 构造方法
     *
     * @param toolInvoker 执行器
     * @param objectSerializer 用于工具调用的参数序列化器
     * @param timeout 超时时间，单位秒
     */
    public WaterFlowAppToolInvoker(@Fit(alias = "storeToolInvoker") ToolInvoker toolInvoker,
            @Fit(alias = "json") ObjectSerializer objectSerializer,
            @Value("${tool-waterflow.app.timeout}") long timeout) {
        super(toolInvoker);
        this.objectSerializer = objectSerializer;
        this.timeout = TimeUnit.SECONDS.toMillis(timeout < 0 ? DEFAULT_TIMEOUT_SECONDS : timeout);
    }

    @Override
    public String invoke(ToolCall toolCall, Map<String, Object> toolContext) {
        Request request = new Request(this.getDecorated(), this.addDynamicParams(toolCall, toolContext), toolContext);
        this.requests.put(toolCall.id(), request);
        try {
            request.post();
            return request.await(this.timeout);
        } finally {
            this.requests.remove(toolCall.id());
        }
    }

    @Override
    public boolean match(ToolData toolData) {
        return toolData.getRunnables().containsKey(WaterFlowToolConst.APP_RUNNABLE_NAME);
    }

    @Override
    public ToolInfo getToolInfo(ToolData toolData) {
        ToolInfo toolInfo = this.getDecorated().getToolInfo(toolData);
        DefaultValueFilterToolInfo.getFilterSchema(toolInfo.parameters());
        return toolInfo;
    }

    @Fitable(INVOKER_CALLBACK_FITABLE_ID)
    @Override
    public void callback(List<Map<String, Object>> flowDataList) {
        Validation.isTrue(flowDataList.size() == 1, "The callback data size is not 1.");
        Map<String, Object> businessData = FlowDataUtils.getBusinessData(flowDataList.get(0));
        Map<String, Object> contextData = FlowDataUtils.getContextData(flowDataList.get(0));
        String instanceId = ObjectUtils.cast(businessData.get(AippConst.BS_AIPP_INST_ID_KEY));
        String parentInstanceId = ObjectUtils.cast(businessData.get(AippConst.PARENT_INSTANCE_ID));
        String toolCallId = ObjectUtils.cast(businessData.get(TOOL_CALL_ID));
        LOGGER.info("The waterflow app tool is finished, toolCallId={}, instanceId={}, parentInstanceId={}",
                toolCallId,
                instanceId,
                parentInstanceId);
        Request request = this.requests.get(toolCallId);
        if (request == null) {
            LOGGER.error("Can not find the request, toolCallId={}, instanceId={}, parentInstanceId={}",
                    toolCallId,
                    instanceId,
                    parentInstanceId);
            return;
        }
        List<Map<String, Object>> executeInfo =
                FlowDataUtils.getExecuteInfo(businessData, FlowDataUtils.getNodeId(contextData));
        if (CollectionUtils.isEmpty(executeInfo)) {
            LOGGER.error("Can not find the response. toolCallId={}, instanceId={}, parentInstanceId={}",
                    toolCallId,
                    instanceId,
                    parentInstanceId);
            FlowErrorInfo errorInfo = new FlowErrorInfo();
            errorInfo.setErrorCode(ErrorCodes.UN_EXCEPTED_ERROR.getErrorCode());
            errorInfo.setErrorMessage("No response");
            request.setError(errorInfo);
            return;
        }
        // 结束节点上的入参为最终出参
        request.setResponse(this.objectSerializer.serialize(executeInfo.get(executeInfo.size() - 1)
                .get(FlowDataConstant.EXECUTE_INPUT_KEY)));
    }

    @Fitable(INVOKER_CALLBACK_FITABLE_ID)
    @Override
    public void handleException(String nodeId, List<Map<String, Object>> flowDataList, FlowErrorInfo errorInfo) {
        Validation.isTrue(flowDataList.size() == 1, "The exception data size is not 1.");
        Map<String, Object> businessData = FlowDataUtils.getBusinessData(flowDataList.get(0));
        String instanceId = ObjectUtils.cast(businessData.get(AippConst.BS_AIPP_INST_ID_KEY));
        String parentInstanceId = ObjectUtils.cast(businessData.get(AippConst.PARENT_INSTANCE_ID));
        String toolCallId = ObjectUtils.cast(businessData.get(TOOL_CALL_ID));
        LOGGER.info("The waterflow app tool has error, toolCallId={}, instanceId={}, parentInstanceId={}, error={}",
                toolCallId,
                instanceId,
                parentInstanceId,
                errorInfo.getErrorMessage());
        Request request = this.requests.get(toolCallId);
        if (request == null) {
            LOGGER.error("Can not find the request, toolCallId={}, instanceId={}, parentInstanceId={}",
                    toolCallId,
                    instanceId,
                    parentInstanceId);
            return;
        }
        request.setError(errorInfo);
    }

    private ToolCall addDynamicParams(ToolCall toolCall, Map<String, Object> toolContext) {
        Map<String, Object> parameters = this.objectSerializer.deserialize(toolCall.arguments(),
                TypeUtils.parameterized(Map.class, new Type[] {String.class, Object.class}));
        Map<String, Object> inputParams = cast(Validation.notNull(parameters.get(WaterFlowToolConst.INPUT_PARAMS_KEY),
                "The inputParams is null."));
        inputParams.putAll(toolContext);
        inputParams.put(TOOL_CALL_ID, toolCall.id());
        inputParams.put(AippConst.PARENT_CALLBACK_ID, INVOKER_CALLBACK_FITABLE_ID);
        inputParams.put(AippConst.PARENT_EXCEPTION_FITABLE_ID, INVOKER_CALLBACK_FITABLE_ID);
        return ToolCall.custom()
                .id(toolCall.id())
                .name(toolCall.name())
                .arguments(this.objectSerializer.serialize(parameters))
                .build();
    }

    /**
     * 通过 store 调用应用的请求的封装
     *
     * @author songyongtan
     * @since 2024/12/26
     */
    private class Request {
        private final ToolInvoker invoker;

        private final ToolCall toolCall;

        private final Map<String, Object> callContext;

        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        private String response = null;

        private FlowErrorInfo error = null;

        /**
         * 构造函数
         *
         * @param invoker 执行器
         * @param toolCall 目标工具调用元数据
         * @param callContext 调用上下文
         */
        public Request(ToolInvoker invoker, ToolCall toolCall, Map<String, Object> callContext) {
            this.invoker = invoker;
            this.toolCall = toolCall;
            this.callContext = callContext;
        }

        /**
         * 发起请求
         */
        public void post() {
            this.invoker.invoke(this.toolCall, this.callContext);
        }

        /**
         * 等待结果
         *
         * @param timeoutMs 最大等待时间，单位毫秒
         * @return 执行结果
         */
        public String await(long timeoutMs) {
            boolean isWaited;
            try {
                isWaited = this.countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
            if (!isWaited) {
                throw new TimeoutException(StringUtils.format("Invocation timeout. [toolUniqueName={0}]",
                        this.toolCall.name()));
            }
            if (this.error != null) {
                throw new FitException(this.error.getErrorCode(), this.error.getErrorMessage());
            }
            return this.response;
        }

        /**
         * 设置回应信息。如果应用执行完成，通过该接口完成请求，表示请求获取到结果
         *
         * @param response 回应信息
         */
        public void setResponse(String response) {
            this.response = response;
            this.countDownLatch.countDown();
        }

        /**
         * 设置异常信息。如果应用执行异常，通过该接口完成请求，表示请求异常
         *
         * @param errorInfo 异常对象
         */
        public void setError(FlowErrorInfo errorInfo) {
            this.error = errorInfo;
            this.countDownLatch.countDown();
        }
    }
}
