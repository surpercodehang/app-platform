/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.service;

import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fitframework.annotation.Genericable;
import modelengine.jade.carver.ListResult;
import modelengine.jade.store.entity.query.AppQuery;
import modelengine.jade.store.entity.transfer.AppData;
import modelengine.jade.store.entity.transfer.AppPublishData;

/**
 * 应用的服务接口类。
 *
 * @author 鲁为
 * @since 2024-06-15
 */
public interface AppService {
    /**
     * 发布应用。
     *
     * @param appData 表示待增加的工具信息的 {@link AppData}。
     * @return 表示添加后应用的唯一标识的 {@link String}。
     */
    @Genericable(id = "modelengine.jade.store.app.publishApp")
    String publishApp(AppPublishData appData);

    /**
     * 查询插件。
     *
     * @param pluginQuery 表示动态查询条件的 {@link AppQuery}。
     * @return 插件数据的封装类的 {@link ListResult}{@code <}{@link AppData}{@code >}。
     */
    @Genericable(id = "modelengine.jade.store.app.getApps")
    ListResult<AppPublishData> getApps(AppQuery pluginQuery);

    /**
     * 基于应用的唯一标识查询某个应用。
     *
     * @param toolUniqueName 表示应用的唯一标识的 {@link String}。
     * @return 表示应用详细信息的 {@link ToolData}。
     */
    @Genericable(id = "modelengine.jade.store.app.getApp.byUniqueName")
    AppPublishData getApp(String toolUniqueName);

    /**
     * 删除插件。
     *
     * @param toolUniqueName 表示待删除插件唯一标识的 {@link String}。
     * @return 表示删除插件的唯一标识名或失败提示的 {@link String}。
     */
    @Genericable(id = "modelengine.jade.store.app.deleteApp")
    String deleteApp(String toolUniqueName);
}
