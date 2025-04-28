/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.upload.support.processor;

import static modelengine.fel.tool.info.schema.PluginSchema.PLUGINS;
import static modelengine.fel.tool.info.schema.ToolsSchema.DEFINITIONS;
import static modelengine.fel.tool.info.schema.ToolsSchema.TOOLS;

import modelengine.fitframework.annotation.Component;

/**
 * 处理器工厂。
 *
 * @author 李金绪
 * @since 2024-10-29
 */
@Component
public class ProcessorFactory {
    private final PluginProcessor pluginProcessor;
    private final DefinitionProcessor defProcessor;
    private final ToolProcessor toolProcessor;

    /**
     * 用于创建一个处理器工厂实例。
     *
     * @param pluginProcessor 表示插件处理器的 {@link PluginProcessor}。
     * @param defProcessor 表示定义处理器的 {@link DefinitionProcessor}。
     * @param toolProcessor 表示工具处理器的 {@link ToolProcessor}。
     */
    public ProcessorFactory(PluginProcessor pluginProcessor, DefinitionProcessor defProcessor,
            ToolProcessor toolProcessor) {
        this.pluginProcessor = pluginProcessor;
        this.defProcessor = defProcessor;
        this.toolProcessor = toolProcessor;
    }

    /**
     * 根据传入的类型参数，返回对应的处理器实例。
     *
     * @param type 表示处理器类型的 {@link String}。
     * @return 表示处理器实例的 {@link Processor}.
     */
    public Processor createInstance(String type) {
        return switch (type) {
            case PLUGINS -> this.pluginProcessor;
            case TOOLS -> this.toolProcessor;
            case DEFINITIONS -> this.defProcessor;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
