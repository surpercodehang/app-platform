/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.deploy.service.support;

import static modelengine.fel.tool.ToolSchema.PROPERTIES_TYPE;
import static modelengine.fel.tool.info.schema.ToolsSchema.FIT;
import static modelengine.fel.tool.info.schema.ToolsSchema.FITABLE_ID;
import static modelengine.fel.tool.info.schema.ToolsSchema.GENERICABLE_ID;
import static modelengine.fitframework.broker.GenericableMetadata.DEFAULT_VERSION;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.info.schema.PluginSchema;
import modelengine.fit.service.RegistryService;
import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fit.service.entity.FitableInfo;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.SecurityUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.ThreadUtils;
import modelengine.jade.carver.ListResult;
import modelengine.jade.common.exception.ModelEngineException;
import modelengine.jade.service.annotations.CarverSpan;
import modelengine.jade.store.code.PluginRetCode;
import modelengine.jade.store.entity.query.PluginToolQuery;
import modelengine.jade.store.entity.transfer.PluginData;
import modelengine.jade.store.entity.transfer.PluginToolData;
import modelengine.jade.store.service.DeployService;
import modelengine.jade.store.service.PluginService;
import modelengine.jade.store.service.PluginToolService;
import modelengine.jade.store.service.support.DeployStatus;
import modelengine.jade.store.tool.deploy.config.PluginDeployQueryConfig;
import modelengine.jade.store.tool.deploy.service.PathGenerationStrategy;
import modelengine.jade.store.tool.deploy.util.PluginDeployManagementUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 表示插件部署服务默认实现。
 *
 * @author 杭潇
 * @since 2025-01-10
 */
@Component
public class DefaultDeployService implements DeployService {
    private static final Logger log = Logger.get(DefaultDeployService.class);
    private static final String OR = "OR";
    private static final int PAGE_SIZE = 100;

    private final PluginService pluginService;
    private final RegistryService registryService;
    private final PluginToolService pluginToolService;
    private final PluginDeployQueryConfig pluginDeployQueryConfig;

    /**
     * 表示插件部署的构造函数。
     *
     * @param pluginService 表示插件服务的 {@link PluginService}。
     * @param registryService 表示注册中心的 {@link RegistryService}。
     * @param pluginToolService 表示插件工具服务的 {@link PluginToolService}。
     * @param pluginDeployQueryConfig 表示插件部署状态查询配置参数的 {@link PluginDeployQueryConfig}。
     */
    public DefaultDeployService(PluginService pluginService, RegistryService registryService,
            PluginToolService pluginToolService, PluginDeployQueryConfig pluginDeployQueryConfig) {
        this.pluginService = notNull(pluginService, "The plugin service cannot be null.");
        this.pluginToolService = notNull(pluginToolService, "The plugin tool service cannot be null.");
        this.registryService = notNull(registryService, "The registry service cannot be null.");
        this.pluginDeployQueryConfig =
                notNull(pluginDeployQueryConfig, "The plugin deploy query config cannot be null.");
    }

    @Override
    @CarverSpan("operation.store.plugin.deploy")
    public void deploy(PluginData pluginData, String pluginFullName, String pluginId) {
        log.info("Start deploy plugin. [pluginId={}]", pluginId);
        Path deployPath = PluginDeployManagementUtils.generateDeployPath(pluginFullName,
                this.pluginDeployQueryConfig.getToolsPath()).resolve(pluginFullName);
        Optional<Path> persistentPath = getPersistentPath(pluginData, pluginFullName, pluginId);
        if (!persistentPath.isPresent()) {
            return;
        }
        try {
            FileUtils.ensureDirectory(deployPath.getParent().toFile());
            Files.copy(persistentPath.get().resolve(pluginFullName), deployPath, StandardCopyOption.REPLACE_EXISTING);
            List<FitableInfo> fitableInfos = this.pluginToolService.getPluginTools(pluginId)
                    .stream()
                    .map(this::getFitableInfo)
                    .collect(Collectors.toList());
            if (this.queryToolsRegisterResult(fitableInfos)) {
                this.pluginService.updateDeployStatus(Collections.singletonList(pluginId), DeployStatus.DEPLOYED);
            } else {
                PluginDeployManagementUtils.undeployPlugin(pluginId, this.pluginService, this.pluginDeployQueryConfig);
                this.pluginService.updateDeployStatus(Collections.singletonList(pluginId),
                        DeployStatus.DEPLOYMENT_FAILED);
            }
        } catch (IOException e) {
            log.error("Failed to deploy plugin. [pluginFile={}]", pluginFullName, e);
            PluginDeployManagementUtils.undeployPlugin(pluginId, this.pluginService, this.pluginDeployQueryConfig);
            this.pluginService.updateDeployStatus(Collections.singletonList(pluginId), DeployStatus.DEPLOYMENT_FAILED);
        }
    }

    private Optional<Path> getPersistentPath(PluginData pluginData, String pluginFullName, String pluginId) {
        Path persistentPath = this.generatePersistentPath(pluginData);
        if (!this.completenessCheck(persistentPath.resolve(pluginFullName).toFile(),
                this.getChecksumFromPluginData(pluginData))) {
            log.error("Completeness check failed before deploy. [pluginFile={}]", pluginId);
            this.pluginService.updateDeployStatus(Collections.singletonList(pluginId), DeployStatus.DEPLOYMENT_FAILED);
            return Optional.empty();
        }
        List<PluginToolData> pluginToolDataList = pluginData.getPluginToolDataList();
        for (PluginToolData pluginToolData : pluginToolDataList) {
            Map<String, Object> runnables = pluginToolData.getRunnables();
            if (runnables.isEmpty()) {
                return Optional.empty();
            }
            Object fit = runnables.get(FIT);
            if (fit != null) {
                Map<String, Object> fitMap = cast(fit);
                this.validateFitableExist(cast(fitMap.get(GENERICABLE_ID)), cast(fitMap.get(FITABLE_ID)));
            }
        }
        return Optional.of(persistentPath);
    }

    private Path generatePersistentPath(PluginData pluginData) {
        Map<String, Object> extension = pluginData.getExtension();
        String type = PluginDeployManagementUtils.requireStringInMapObject(extension.get(PROPERTIES_TYPE));

        PathGenerationStrategy strategy;
        if (StringUtils.equalsIgnoreCase(type, PluginSchema.JAVA)) {
            strategy = new JavaPathGenerationStrategy();
        } else if (StringUtils.equalsIgnoreCase(type, PluginSchema.PYTHON)) {
            strategy = new PythonPathGenerationStrategy();
        } else {
            strategy = new UnsupportedLanguageStrategy(type);
        }
        return strategy.generatePath(extension);
    }

    private boolean queryToolsRegisterResult(List<FitableInfo> fitableInfos) {
        long startTimestamp = System.currentTimeMillis();
        while (!this.isQueryTimeout(startTimestamp)) {
            List<FitableAddressInstance> result = this.registryService.queryFitables(fitableInfos, "");
            if (result.size() == fitableInfos.size() && result.stream()
                    .noneMatch(info -> info.getApplicationInstances().isEmpty())) {
                return true;
            }
            ThreadUtils.sleep(this.pluginDeployQueryConfig.getInterval() * 1000L);
        }
        return false;
    }

    private boolean isQueryTimeout(long startTimestamp) {
        return System.currentTimeMillis() - startTimestamp > this.pluginDeployQueryConfig.getTimeout() * 1000L;
    }

    private boolean completenessCheck(File pluginFile, String expectCheckSum) {
        String fileChecksum = SecurityUtils.signatureOf(pluginFile, "sha-256", 1024);
        return expectCheckSum.equals(fileChecksum);
    }

    private String getChecksumFromPluginData(PluginData pluginData) {
        Map<String, Object> extension = pluginData.getExtension();
        return PluginDeployManagementUtils.requireStringInMapObject(extension.get(PluginSchema.CHECKSUM));
    }

    private FitableInfo getFitableInfo(PluginToolData pluginToolData) {
        Map<String, Object> runnable = cast(pluginToolData.getRunnables().get(FIT));
        FitableInfo fitableInfo = new FitableInfo();
        fitableInfo.setFitableId(PluginDeployManagementUtils.requireStringInMapObject(runnable.get(FITABLE_ID)));
        fitableInfo.setFitableVersion(DEFAULT_VERSION);
        fitableInfo.setGenericableId(PluginDeployManagementUtils.requireStringInMapObject(runnable.get(GENERICABLE_ID)));
        fitableInfo.setGenericableVersion(DEFAULT_VERSION);
        return fitableInfo;
    }

    private void validateFitableExist(String genericableId, String fitableId) {
        for (int i = 0; i < this.pluginDeployQueryConfig.getMaxToolSize(); i++) {
            ListResult<PluginToolData> pluginTools =
                    this.pluginToolService.getPluginTools(new PluginToolQuery.Builder().toolName(null)
                            .includeTags(new HashSet<>())
                            .excludeTags(new HashSet<>())
                            .mode(OR)
                            .offset(i * PAGE_SIZE)
                            .limit(PAGE_SIZE)
                            .version(null)
                            .isDeployed(true)
                            .build());
            if (pluginTools == null || pluginTools.getCount() < 1) {
                return;
            }
            for (PluginToolData pluginToolData : pluginTools.getData()) {
                Map<String, Object> runnables = pluginToolData.getRunnables();
                if (runnables.isEmpty()) {
                    return;
                }
                Object fit = runnables.get(FIT);
                if (fit == null) {
                    return;
                }
                Map<String, Object> fitMap = cast(fit);
                Object deployedGenericableId =
                        notNull(fitMap.get(GENERICABLE_ID), "The genericable id cannot be null.");
                Object deployedFitableId = fitMap.get(FITABLE_ID);
                if (deployedFitableId == null) {
                    return;
                }
                if (deployedFitableId.equals(fitableId) && deployedGenericableId.equals(genericableId)) {
                    throw new ModelEngineException(PluginRetCode.PLUGIN_DEPLOY_FAILED,
                            StringUtils.format("The tool has been deployed. [genericableId={0}, fitableId={1}]",
                                    genericableId,
                                    fitableId));
                }
            }
        }
    }
}
