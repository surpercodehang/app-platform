/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.exporter.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.jade.carver.exporter.repository.stub.SpanExporterSub;
import modelengine.jade.service.SpanExporterRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * {@link SpanExporterRepositoryStarter} 的测试。
 *
 * @author 刘信宏
 * @since 2024-07-25
 */
public class SpanExporterRepositoryStarterTest {
    private SpanExporterRepository getExporterContainer(SpanExporterRepositoryStarter repositoryStarter)
            throws NoSuchFieldException, IllegalAccessException {
        Field exportersContainer = repositoryStarter.getClass().getDeclaredField("exportersRepository");
        exportersContainer.setAccessible(true);
        return ObjectUtils.cast(exportersContainer.get(repositoryStarter));
    }

    @FitTestWithJunit(includeClasses = {
            SpanExporterRepositoryStarter.class, DefaultSpanExporterRepository.class, SpanExporterSub.class,
            SpanProcessorConfig.class
    })
    @Nested
    @DisplayName("测试插件带有 SpanExporter 的场景")
    class ContainerStarterWithExporterTest {
        @Fit
        private SpanExporterRepositoryStarter repositoryStarter;
        @Fit
        private Plugin plugin;

        @AfterEach
        void tearDown() {
            this.repositoryStarter.onPluginStopping(this.plugin);
        }

        @Test
        @DisplayName("注册/注销 exporter 成功。")
        public void shouldOkWhenRegisterExporterAfterRegistering() throws NoSuchFieldException, IllegalAccessException {
            // 插件启动，自动触发一次 onPluginStarted
            SpanExporterRepository exporterContainer = getExporterContainer(this.repositoryStarter);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(1);

            this.repositoryStarter.onPluginStopping(this.plugin);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(0);

            this.repositoryStarter.onPluginStarted(this.plugin);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(1);
        }

        @Test
        @DisplayName("超过最大限制，注册 exporter 失败。")
        public void shouldFailWhenRegisterExporterOverMaxSize() throws NoSuchFieldException, IllegalAccessException {
            this.repositoryStarter.onPluginStopping(this.plugin);
            SpanExporterRepository exporterContainer = getExporterContainer(this.repositoryStarter);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(0);

            for (int i = 0; i < 10; i++) {
                exporterContainer.register(new SpanExporterSub());
            }
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(10);
            assertThatThrownBy(() -> this.repositoryStarter.onPluginStarting(this.plugin)).isInstanceOf(
                    IllegalStateException.class).hasMessage("The exporters cannot greater than 10.");
        }
    }

    @FitTestWithJunit(includeClasses = {
            SpanExporterRepositoryStarter.class, DefaultSpanExporterRepository.class, SpanProcessorConfig.class
    })
    @Nested
    @DisplayName("测试插件无 SpanExporter 的场景")
    class ContainerStarterWithoutExporterTest {
        @Fit
        private SpanExporterRepositoryStarter containerStarter;

        @Test
        @DisplayName("插件初始化，无 exporter，手动注册 exporter 成功。")
        public void shouldOkWhenPluginInitWithoutExporter() throws NoSuchFieldException, IllegalAccessException {
            SpanExporterRepository exporterContainer = getExporterContainer(this.containerStarter);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(0);

            SpanExporterSub exporterStub = new SpanExporterSub();
            exporterContainer.register(exporterStub);
            assertThat(exporterContainer.get(exporter -> true).size()).isEqualTo(1);

            exporterContainer.unregister(exporterStub);
        }
    }
}
