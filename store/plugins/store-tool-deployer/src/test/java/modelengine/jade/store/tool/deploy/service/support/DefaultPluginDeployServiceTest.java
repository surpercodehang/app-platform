/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.deploy.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import modelengine.fit.service.RegistryService;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.util.FileUtils;
import modelengine.jade.carver.ListResult;
import modelengine.jade.store.entity.query.PluginQuery;
import modelengine.jade.store.entity.transfer.PluginData;
import modelengine.jade.store.entity.transfer.PluginToolData;
import modelengine.jade.store.service.DeployService;
import modelengine.jade.store.service.PluginService;
import modelengine.jade.store.service.PluginToolService;
import modelengine.jade.store.service.support.DeployStatus;
import modelengine.jade.store.tool.deploy.config.PluginDeployQueryConfig;
import modelengine.jade.store.tool.deploy.config.RegistryQueryPoolConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 表示 {@link DefaultPluginDeployService} 的单测。
 *
 * @author 杭潇
 * @author 罗帅
 * @since 2024-10-07
 */
@ExtendWith(MockitoExtension.class)
class DefaultPluginDeployServiceTest {
    @Mock
    private PluginService mockPluginService;

    @Mock
    private RegistryService mockRegistryService;

    @Mock
    private PluginToolService mockPluginToolService;

    private DefaultPluginDeployService pluginDeployService;

    @Mock
    private FitRuntime fitRuntime;

    @BeforeEach
    void setUp() {
        PluginDeployQueryConfig pluginDeployQueryConfig = new PluginDeployQueryConfig();
        pluginDeployQueryConfig.setInterval(10);
        pluginDeployQueryConfig.setTimeout(60);
        pluginDeployQueryConfig.setToolsPath("/demo");
        pluginDeployQueryConfig.setMaxToolSize(20);
        RegistryQueryPoolConfig registryQueryPoolConfig = new RegistryQueryPoolConfig();
        registryQueryPoolConfig.setCorePoolSize(20);
        registryQueryPoolConfig.setMaximumPoolSize(20);
        registryQueryPoolConfig.setWorkQueueCapacity(10);
        DeployService deployService = new DefaultDeployService(this.mockPluginService,
                this.mockRegistryService,
                this.mockPluginToolService,
                pluginDeployQueryConfig);
        this.pluginDeployService = new DefaultPluginDeployService(this.mockPluginService,
                registryQueryPoolConfig,
                pluginDeployQueryConfig,
                deployService);
    }

    @AfterEach
    void teardown() {
        Path targetFile = Paths.get("src/test/resources/tool.tar");
        if (Files.exists(targetFile)) {
            FileUtils.delete(targetFile.toFile());
        }
        Path testFile = Paths.get("src/test/resources/test/");
        if (Files.exists(testFile)) {
            FileUtils.delete(testFile.toFile());
        }
    }

    private PluginData mockPluginData() {
        final PluginData pluginData = new PluginData();
        pluginData.setPluginId("pluginId");
        pluginData.setPluginName("pluginName");
        Map<String, Object> extension = new HashMap<>();
        extension.put("pluginFullName", "pluginFullName.zip");
        extension.put("type", "python");
        extension.put("name", "name");
        extension.put("pluginName", "pluginName");
        pluginData.setExtension(extension);
        pluginData.setDeployStatus(DeployStatus.UNDEPLOYED.name());
        pluginData.setPluginToolDataList(Collections.singletonList(this.mockPluginToolData()));
        pluginData.setBuiltin(false);
        return pluginData;
    }

    private PluginToolData mockPluginToolData() {
        final PluginToolData pluginToolData = new PluginToolData();
        pluginToolData.setDescription("description");
        pluginToolData.setSchema(new HashMap<>());
        Map<String, Object> runnables = new HashMap<>();
        Map<String, Object> fitables = new HashMap<>();
        fitables.put("fitableId", "multi_fid");
        fitables.put("genericableId", "multi_gid");
        runnables.put("FIT", fitables);
        pluginToolData.setRunnables(runnables);
        pluginToolData.setTags(new HashSet<>(Collections.singletonList("value")));
        pluginToolData.setName("name");
        return pluginToolData;
    }

    @Test
    @DisplayName("部署文件符合预期")
    void testDeployPlugins() {
        when(this.mockPluginService.getPlugin(Mockito.anyString())).thenReturn(mockPluginData());
        when(this.mockPluginService.getPlugins(DeployStatus.DEPLOYED)).thenReturn(Collections.singletonList(
                mockPluginData()));
        this.pluginDeployService.deployPlugins(Collections.singletonList("value"));
        verify(this.mockPluginService).updateDeployStatus(Collections.singletonList("value"), DeployStatus.DEPLOYING);
    }

    @Test
    @DisplayName("给定有效的数据，校验runnables逻辑结果正常")
    void givenValidDataThenParseValueSuccessfully() throws NoSuchMethodException {
        Method validateToolRunnablesMethod =
                DefaultPluginDeployService.class.getDeclaredMethod("validateRunnable", Map.class, Set.class);
        validateToolRunnablesMethod.setAccessible(true);

        Map<String, Object> fit = new HashMap<>();
        Map<String, Object> fitInfo = new HashMap<>();
        fitInfo.put("genericableId", "modelengine.fit.test_demo");
        fitInfo.put("fitableId", "default_fitable_id");
        fit.put("FIT", fitInfo);
        assertDoesNotThrow(() -> validateToolRunnablesMethod.invoke(this.pluginDeployService, fit, new HashSet<>()));
    }

    @Test
    @DisplayName("插件信息为空时部署插件符合预期")
    void testDeployPlugins_PluginServiceGetPluginReturnsNull() {
        when(this.mockPluginService.getPlugin("pluginId")).thenReturn(null);
        assertThatThrownBy(() -> {
            List<String> values = Collections.singletonList("value");
            this.pluginDeployService.deployPlugins(values);
        }).isInstanceOf(PotentialStubbingProblem.class);
    }

    @Test
    @DisplayName("部署插件时查询插件状态成功")
    void testDeployPlugins_PluginServiceGetPluginsReturnsNoItems() {
        when(this.mockPluginService.getPlugin(Mockito.anyString())).thenReturn(mockPluginData());
        when(this.mockPluginService.getPlugins(DeployStatus.DEPLOYED)).thenReturn(Collections.emptyList());
        this.pluginDeployService.deployPlugins(Collections.singletonList("value"));
        verify(this.mockPluginService).updateDeployStatus(Collections.singletonList("value"), DeployStatus.DEPLOYING);
    }

    @Test
    @DisplayName("获取已部署插件数量成功")
    void testQueryCountByDeployStatus() {
        when(this.mockPluginService.getPluginsCount(DeployStatus.DEPLOYED)).thenReturn(0);
        final int result = this.pluginDeployService.queryCountByDeployStatus(DeployStatus.DEPLOYED);
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("通过部署状态查询插件信息符合预期")
    void testQueryPluginsByDeployStatus() {
        final PluginData pluginData1 = new PluginData();
        pluginData1.setPluginId("pluginId");
        pluginData1.setPluginName("pluginName");
        pluginData1.setExtension(new HashMap<>());
        pluginData1.setDeployStatus("deployStatus");
        final PluginToolData pluginToolData = new PluginToolData();
        pluginToolData.setDescription("description");
        pluginToolData.setSchema(new HashMap<>());
        pluginToolData.setRunnables(new HashMap<>());
        pluginToolData.setTags(new HashSet<>(Collections.singletonList("value")));
        pluginToolData.setName("name");
        pluginData1.setPluginToolDataList(Collections.singletonList(pluginToolData));
        final List<PluginData> pluginData = Collections.singletonList(pluginData1);
        when(this.mockPluginService.getPlugins(DeployStatus.DEPLOYED)).thenReturn(pluginData);
        final List<PluginData> result = this.pluginDeployService.queryPluginsByDeployStatus(DeployStatus.DEPLOYED);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("查询无满足要求的插件时返回空列表")
    void testQueryPluginsByDeployStatus_PluginServiceReturnsNoItems() {
        when(this.mockPluginService.getPlugins(DeployStatus.DEPLOYED)).thenReturn(Collections.emptyList());
        final List<PluginData> result = this.pluginDeployService.queryPluginsByDeployStatus(DeployStatus.DEPLOYED);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    private ListResult<PluginData> mockPluginDataResult() {
        return new ListResult<>(Collections.singletonList(mockPluginData()), 1);
    }

    @Test
    @DisplayName("测试初始化插件状态功能正常")
    void testInitDeployStatus() {
        when(this.mockPluginService.getPlugins(any(DeployStatus.class))).thenReturn(Collections.singletonList(
                mockPluginData()));
        when(this.mockPluginService.getPlugins(any(PluginQuery.class))).thenReturn(mockPluginDataResult());
        when(this.mockPluginService.getPlugin(Mockito.anyString())).thenReturn(mockPluginData());
        this.pluginDeployService.onRuntimeStarted(fitRuntime);
        verify(this.mockPluginService).updateDeployStatus(Mockito.anyList(), eq(DeployStatus.UNDEPLOYED));
    }
}