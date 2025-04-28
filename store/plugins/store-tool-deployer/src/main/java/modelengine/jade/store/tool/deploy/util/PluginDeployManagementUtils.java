/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.deploy.util;

import modelengine.fel.tool.info.schema.PluginSchema;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.FileUtils;
import modelengine.jade.store.entity.transfer.PluginData;
import modelengine.jade.store.service.DeployService;
import modelengine.jade.store.service.PluginService;
import modelengine.jade.store.tool.deploy.config.PluginDeployQueryConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 表示插件部署操作管理类。
 *
 * @author 杭潇
 * @since 2025-01-10
 */
public class PluginDeployManagementUtils {
    private static final Logger log = Logger.get(PluginDeployManagementUtils.class);

    /**
     * 若 Object 数据是 String 类型的实例则转换为 String 值。
     *
     * @param object 表示待转换数据的 {@link Object}。
     * @return 转换后值的 {@link String}。
     */
    public static String requireStringInMapObject(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        throw new IllegalStateException("Object can not cast to string.");
    }

    /**
     * 根据插件 Id 取消部署插件。
     *
     * @param pluginId 表示待取消部署的插件 Id 的 {@link String}。
     * @param pluginService 表示插件部署服务的 {@link DeployService}。
     * @param pluginDeployQueryConfig 表示插件部署状态查询配置参数的 {@link PluginDeployQueryConfig}。
     */
    public static void undeployPlugin(String pluginId, PluginService pluginService,
            PluginDeployQueryConfig pluginDeployQueryConfig) {
        PluginData pluginData = pluginService.getPlugin(pluginId);
        Path deployedPath = Paths.get(generateDeployPath(getPluginFullName(pluginData),
                pluginDeployQueryConfig.getToolsPath()).toString(), getPluginFullName(pluginData));
        try {
            FileUtils.delete(deployedPath.toFile());
        } catch (IllegalStateException e) {
            log.error("Failed to delete plugin. [pluginName={}]", pluginData.getPluginName(), e);
        }
    }

    /**
     * 表示获取插件的全名。
     *
     * @param pluginData 表示插件数据的 {@link PluginData}。
     * @return 插件全名的 {@link String}。
     */
    public static String getPluginFullName(PluginData pluginData) {
        Map<String, Object> extension = pluginData.getExtension();
        return requireStringInMapObject(extension.get(PluginSchema.PLUGIN_FULL_NAME));
    }

    /**
     * 表示生成插件部署地址。
     *
     * @param toolName 表示插件名的 {@link String}。
     * @param toolPath 表示插件上传的路径的 {@link String}。
     * @return 插件部署地址的 {@link Path}。
     */
    public static Path generateDeployPath(String toolName, String toolPath) {
        return toolName.endsWith(PluginSchema.JAR)
                ? Paths.get(toolPath, PluginSchema.JAVA)
                : Paths.get(toolPath, PluginSchema.PYTHON);
    }
}
