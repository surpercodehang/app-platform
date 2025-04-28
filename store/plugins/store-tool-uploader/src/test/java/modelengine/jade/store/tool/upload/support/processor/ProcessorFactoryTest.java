/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.upload.support.processor;

import static modelengine.fel.tool.info.schema.PluginSchema.PLUGINS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 表示 {@link ProcessorFactory} 的测试类。
 *
 * @author 李金绪
 * @since 2024-10-29
 */
@DisplayName("测试 ProcessorFactory")
public class ProcessorFactoryTest {
    private ProcessorFactory processorFactory;

    @BeforeEach
    void setup() {
        PluginProcessor pluginProcessor = mock(PluginProcessor.class);
        DefinitionProcessor defProcessor = mock(DefinitionProcessor.class);
        ToolProcessor toolProcessor = mock(ToolProcessor.class);
        this.processorFactory = new ProcessorFactory(pluginProcessor, defProcessor, toolProcessor);
    }

    @Test
    @DisplayName("当指定类型时，正确返回对应的处理器")
    void shouldOkWhenGet() {
        Processor processor = this.processorFactory.createInstance(PLUGINS);
        assertThat(processor.getClass()).isEqualTo(PluginProcessor.class);
    }

    @Test
    @DisplayName("当不存在类型时，抛出异常")
    void shouldExWhenNoType() {
        assertThatThrownBy(() -> {
            Processor processor = this.processorFactory.createInstance("test");
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown type: test");
    }
}
