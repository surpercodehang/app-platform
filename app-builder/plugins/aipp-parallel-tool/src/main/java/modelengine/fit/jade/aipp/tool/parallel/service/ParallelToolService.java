/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.service;

import modelengine.fit.jade.aipp.tool.parallel.entities.Config;
import modelengine.fit.jade.aipp.tool.parallel.entities.ToolCall;
import modelengine.fitframework.annotation.Genericable;
import modelengine.fitframework.annotation.Property;
import modelengine.jade.carver.tool.annotation.Group;
import modelengine.jade.carver.tool.annotation.ToolMethod;

import java.util.List;
import java.util.Map;

/**
 * 并行工具调用节点服务接口。
 *
 * @author 宋永坦
 * @since 2025-04-23
 */
@Group(name = "ParallelTool")
public interface ParallelToolService {
    /**
     * 并行工具调用。
     *
     * @param toolCalls 表示待调用的工具列表的 {@link List}{@code <}{@link ToolCall}{@code >}。
     * @param config 表示调用配置的 {@link Config}。
     * @param context 表示调用上下文的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示调用工具列表的返回值的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    @ToolMethod(name = "parallelTool", description = "用于并行执行工具")
    @Genericable("modelengine.fit.jade.aipp.tool.parallel")
    Map<String, Object> call(@Property(description = "并行调用的工具列表", required = true) List<ToolCall> toolCalls,
            @Property(description = "并行调用的配置") Config config,
            @Property(description = "调用时的上下文信息") Map<String, Object> context);
}
