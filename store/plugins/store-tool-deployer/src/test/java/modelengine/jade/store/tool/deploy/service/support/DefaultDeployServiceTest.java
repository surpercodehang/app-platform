/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.deploy.service.support;

import static modelengine.fitframework.util.ObjectUtils.cast;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import modelengine.fit.service.RegistryService;
import modelengine.fit.service.entity.ApplicationInstance;
import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fit.service.entity.FitableInfo;
import modelengine.jade.carver.ListResult;
import modelengine.jade.store.entity.query.PluginToolQuery;
import modelengine.jade.store.entity.transfer.PluginToolData;
import modelengine.jade.store.service.DeployService;
import modelengine.jade.store.service.PluginService;
import modelengine.jade.store.service.PluginToolService;
import modelengine.jade.store.tool.deploy.config.PluginDeployQueryConfig;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 表示 {@link DefaultDeployService} 的单测。
 *
 * @author 杭潇
 * @since 2025-01-11
 */
@ExtendWith(MockitoExtension.class)
class DefaultDeployServiceTest {
    @Mock
    private PluginService mockPluginService;

    @Mock
    private RegistryService mockRegistryService;

    @Mock
    private PluginToolService mockPluginToolService;

    private DeployService deployService;

    @BeforeEach
    void setUp() {
        PluginDeployQueryConfig pluginDeployQueryConfig = new PluginDeployQueryConfig();
        pluginDeployQueryConfig.setInterval(10);
        pluginDeployQueryConfig.setTimeout(60);
        pluginDeployQueryConfig.setToolsPath("/demo");
        pluginDeployQueryConfig.setMaxToolSize(20);
        this.deployService = new DefaultDeployService(this.mockPluginService,
                this.mockRegistryService,
                this.mockPluginToolService,
                pluginDeployQueryConfig);
    }

    @Test
    @DisplayName("获取 FIT 消息符合预期")
    void testGetFitableInfo() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = DefaultDeployService.class.getDeclaredMethod("getFitableInfo", PluginToolData.class);
        method.setAccessible(true);
        Object fitableInfo = method.invoke(this.deployService, this.mockPluginToolData());
        AssertionsForClassTypes.assertThat(fitableInfo).isInstanceOf(FitableInfo.class);
        FitableInfo infoAfterCast = cast(fitableInfo);
        AssertionsForClassTypes.assertThat(infoAfterCast.getFitableVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("给定已经部署的插件信息，校验失败")
    void givenDeployPluginThenValidateFailed() throws NoSuchMethodException {
        Method method =
                DefaultDeployService.class.getDeclaredMethod("validateFitableExist", String.class, String.class);
        method.setAccessible(true);
        String genericableId = "multi_gid";
        String fitableId = "multi_fid";

        ListResult<PluginToolData> listResult =
                new ListResult<>(Collections.singletonList(this.mockPluginToolData()), 100);
        when(this.mockPluginToolService.getPluginTools(any(PluginToolQuery.class))).thenReturn(listResult);
        InvocationTargetException invocationTargetException =
                catchThrowableOfType(() -> method.invoke(this.deployService, genericableId, fitableId),
                        InvocationTargetException.class);
        AssertionsForClassTypes.assertThat(invocationTargetException.getTargetException().getMessage())
                .isEqualTo("Failed to deploy the plugin. [cause=The tool has been deployed. "
                        + "[genericableId=multi_gid, fitableId=multi_fid]]");
    }

    @Test
    @DisplayName("查询插件工具注册结果符合预期")
    void testQueryToolsRegisterResult()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = DefaultDeployService.class.getDeclaredMethod("queryToolsRegisterResult", List.class);
        method.setAccessible(true);
        FitableAddressInstance fitableAddressInstance = getFitableAddressInstance();
        List<FitableAddressInstance> fitableAddressInstances = Collections.singletonList(fitableAddressInstance);
        FitableInfo fitableInfo = new FitableInfo();
        fitableInfo.setGenericableId("genericableId");
        fitableInfo.setGenericableVersion("genericableVersion");
        fitableInfo.setFitableId("fitableId");
        fitableInfo.setFitableVersion("fitableVersion");
        List<FitableInfo> fitables = Collections.singletonList(fitableInfo);
        when(this.mockRegistryService.queryFitables(fitables, "")).thenReturn(fitableAddressInstances);
        Object result = method.invoke(this.deployService, fitables);
        AssertionsForClassTypes.assertThat(result).isInstanceOf(Boolean.class);
        boolean isRegistered = cast(result);
        AssertionsForClassTypes.assertThat(isRegistered).isTrue();
    }

    private static FitableAddressInstance getFitableAddressInstance() {
        final FitableAddressInstance fitableAddressInstance = new FitableAddressInstance();
        final FitableInfo fitable = new FitableInfo();
        fitable.setGenericableId("genericableId");
        fitable.setGenericableVersion("genericableVersion");
        fitable.setFitableId("fitableId");
        fitableAddressInstance.setFitable(fitable);
        final ApplicationInstance applicationInstance = new ApplicationInstance();
        fitableAddressInstance.setApplicationInstances(Collections.singletonList(applicationInstance));
        return fitableAddressInstance;
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
}