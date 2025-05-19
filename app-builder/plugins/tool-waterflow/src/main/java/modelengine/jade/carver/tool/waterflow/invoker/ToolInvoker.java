/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow.invoker;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.model.transfer.ToolData;

import java.util.Map;

/**
 * 大模型工具调用抽象
 *
 * @author songyongtan
 * @since 2024/12/25
 */
public interface ToolInvoker {
    /**
     * 执行工具调用
     *
     * @param toolCall 工具调用元数据
     * @param toolContext 工具调用上下文数据
     * @return 工具执行结果
     */
    String invoke(ToolCall toolCall, Map<String, Object> toolContext);

    /**
     * 判断是否匹配对应的工具调用
     *
     * @param toolData 工具信息
     * @return true-匹配，false-不匹配
     */
    boolean match(ToolData toolData);

    /**
     * 根据工具信息获取 {@link ToolInfo} 对象。
     *
     * @param toolData 工具信息。
     * @return {@link ToolInfo} 对象。
     */
    ToolInfo getToolInfo(ToolData toolData);
}
