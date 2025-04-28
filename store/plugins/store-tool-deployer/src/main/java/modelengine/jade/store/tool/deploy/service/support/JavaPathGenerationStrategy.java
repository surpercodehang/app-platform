/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.deploy.service.support;

import static modelengine.fel.tool.info.schema.PluginSchema.ARTIFACT_ID;
import static modelengine.fel.tool.info.schema.PluginSchema.DOT;
import static modelengine.fel.tool.info.schema.PluginSchema.GROUP_ID;
import static modelengine.fel.tool.info.schema.PluginSchema.UNIQUENESS;

import modelengine.fel.tool.info.schema.PluginSchema;
import modelengine.jade.store.tool.deploy.service.PathGenerationStrategy;
import modelengine.jade.store.tool.deploy.util.PluginDeployManagementUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 表示 Java 插件路径生成策略。
 *
 * @author 杭潇
 * @since 2025-01-13
 */
class JavaPathGenerationStrategy implements PathGenerationStrategy {
    @Override
    public Path generatePath(Map<String, Object> extension) {
        return Paths.get(PERSISTENT_PATH,
                PluginSchema.JAVA,
                PluginDeployManagementUtils.requireStringInMapObject(extension.get(UNIQUENESS + DOT + ARTIFACT_ID)),
                PluginDeployManagementUtils.requireStringInMapObject(extension.get(UNIQUENESS + DOT + GROUP_ID)));
    }
}

