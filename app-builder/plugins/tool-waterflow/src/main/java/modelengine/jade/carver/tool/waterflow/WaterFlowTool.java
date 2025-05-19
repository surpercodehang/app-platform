/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.Tool;

import java.util.Map;

/**
 * 表示 {@link WaterFlowTool} 的自定义实现。
 *
 * @author 王攀博
 * @since 2024-04-18
 */
public class WaterFlowTool implements Tool {
    private final Tool tool;
    private final Tool.Info toolInfo;
    private final Metadata metadata;

    /**
     * 通过 Json 处理工具和工具元数据来初始化 {@link WaterFlowTool} 的新实例。
     *
     * @param tool 表示工作流工具入口调用的真实工具的 {@link Tool}。
     * @param info 表示工具的基本信息的 {@link Tool.Info}。
     * @param metadata 表示工作流工具的元数据信息的 {@link Metadata}。
     * @throws IllegalArgumentException 当 {@code argsTool} 或 {@code metadata} 为 {@code null} 时。
     */
    public WaterFlowTool(Tool tool, Tool.Info info, Tool.Metadata metadata) {
        this.tool = notNull(tool, "The tool cannot be null.");
        this.toolInfo = new DefaultValueFilterToolInfo(info);
        this.metadata = notNull(metadata, "The tool metadata cannot be null.");
    }

    @Override
    public Tool.Info info() {
        return this.toolInfo;
    }

    @Override
    public Metadata metadata() {
        return this.metadata;
    }

    @Override
    public Object execute(Object... args) {
        return this.tool.execute(args);
    }

    @Override
    public Object executeWithJson(String jsonArgs) {
        return this.tool.executeWithJson(jsonArgs);
    }

    @Override
    public Object executeWithJsonObject(Map<String, Object> jsonObject) {
        return this.tool.executeWithJsonObject(jsonObject);
    }

    @Override
    public String prettyExecute(Object... args) {
        return this.tool.prettyExecute(args);
    }

    @Override
    public String prettyExecuteWithJson(String jsonArgs) {
        return this.tool.prettyExecuteWithJson(jsonArgs);
    }

    @Override
    public String prettyExecuteWithJsonObject(Map<String, Object> jsonObject) {
        return this.tool.prettyExecuteWithJsonObject(jsonObject);
    }

    @Override
    public String prettyExecute(Tool converter, Object... args) {
        return this.tool.prettyExecute(converter, args);
    }

    @Override
    public String prettyExecuteWithJson(Tool converter, String jsonArgs) {
        return this.tool.prettyExecuteWithJson(converter, jsonArgs);
    }

    @Override
    public String prettyExecuteWithJsonObject(Tool converter, Map<String, Object> jsonObject) {
        return this.tool.prettyExecuteWithJsonObject(converter, jsonObject);
    }
}
