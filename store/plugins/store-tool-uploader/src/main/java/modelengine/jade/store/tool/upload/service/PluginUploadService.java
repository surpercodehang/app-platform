/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.upload.service;

import modelengine.fel.tool.info.entity.HttpJsonEntity;
import modelengine.fit.http.entity.NamedEntity;

import java.util.List;

/**
 * 插件部署服务接口。
 *
 * @author 罗帅
 * @since 2024-8-13
 */
public interface PluginUploadService {
    /**
     * 上传插件。
     *
     * @param namedEntities 表示插件包的 {@link List}{@code <}{@link NamedEntity}{@code >}。
     * @param toolNames 表示工具名称的 {@link String}。
     */
    void uploadPlugins(List<NamedEntity> namedEntities, List<String> toolNames);

    /**
     * 删除插件。
     *
     * @param pluginId 表示插件唯一标识的 {@link String}。
     * @return 表示删除数量的 {@code int}。
     */
    int deletePlugin(String pluginId);

    /**
     * 上传 http 插件。
     *
     * @param httpEntity 表示 http 插件的 {@link HttpJsonEntity}。
     */
    void uploadHttp(HttpJsonEntity httpEntity);
}