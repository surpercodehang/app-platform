/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow.invoker;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Component;
import modelengine.jade.carver.tool.waterflow.WaterFlowToolConst;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步执行的工具的执行器
 *
 * @author songyongtan
 * @since 2024/12/25
 */
@Component("storeToolInvoker")
public class StoreToolInvoker implements ToolInvoker {
    private final ToolExecuteService toolExecuteService;

    /**
     * 构造方法
     *
     * @param toolExecuteService store的工具执行服务
     */
    public StoreToolInvoker(ToolExecuteService toolExecuteService) {
        this.toolExecuteService = toolExecuteService;
    }

    @Override
    public String invoke(ToolCall toolCall, Map<String, Object> toolContext) {
        return this.storeExecute(toolCall);
    }

    @Override
    public boolean match(ToolData toolData) {
        return !toolData.getRunnables().containsKey(WaterFlowToolConst.APP_RUNNABLE_NAME);
    }

    @Override
    public ToolInfo getToolInfo(ToolData toolData) {
        Map<String, Object> schema = new HashMap<>(toolData.getSchema());
        return ToolInfo.custom()
                .name(toolData.getUniqueName())
                .description(toolData.getDescription())
                .parameters(schema)
                .extensions(new HashMap<>())
                .build();
    }

    /**
     * 通过 store 调用工具
     *
     * @param toolCall 工具调用元数据
     * @return 工具执行结果
     */
    protected String storeExecute(ToolCall toolCall) {
        return this.toolExecuteService.execute(toolCall.name(), toolCall.arguments());
    }
}
