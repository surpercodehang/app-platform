/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.service;

import modelengine.fel.tool.model.ListResult;
import modelengine.fel.tool.model.transfer.DefinitionGroupData;
import modelengine.fitframework.annotation.Genericable;
import modelengine.jade.store.entity.query.ToolQuery;
import modelengine.jade.store.entity.transfer.StoreToolData;

import java.util.List;

/**
 * 具有更多信息的工具的服务接口类。
 *
 * @author 李金绪
 * @since 2024-09-13
 */
public interface StoreToolService {
    /**
     * 根据动态条件准确查询工具列表。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}
     * @return 表示工具列表的 {@link ListResult}{@code <}{@link StoreToolData}{@code >}。
     */
    @Genericable(id = "modelengine.jade.store.tool.getTools.byToolQuery")
    ListResult<StoreToolData> getTools(ToolQuery toolQuery);

    /**
     * 根据动态条件模糊查询工具列表。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}
     * @return 表示工具列表的 {@link ListResult}{@code <}{@link StoreToolData}{@code >}。
     */
    @Genericable(id = "modelengine.jade.store.tool.searchTools.byToolQuery")
    ListResult<StoreToolData> searchTools(ToolQuery toolQuery);

    /**
     * 查询一个工具的所有版本。
     *
     * @param toolQuery 表示查询条件的 {@link ToolQuery}。
     * @return 表示工具版本列表的 {@link ListResult}{@code <}{@link StoreToolData}{@code >}。
     */
    @Genericable(id = "modelengine.jade.store.tool.getAllToolVersions")
    ListResult<StoreToolData> getAllToolVersions(ToolQuery toolQuery);

    /**
     * 基于工具的唯一标识查询某个工具。
     *
     * @param toolUniqueName 表示工具的唯一标识的 {@link String}。
     * @return 表示工具详细信息的 {@link StoreToolData}。
     */
    @Genericable(id = "modelengine.jade.store.tool.getTool.byUniqueName")
    StoreToolData getTool(String toolUniqueName);

    /**
     * 查询工具的某一个版本。
     *
     * @param toolUniqueName 表示工具的唯一标识的 {@link String}。
     * @param version 表示工具的版本的 {@link String}。
     * @return 表示工具详细信息的 {@link StoreToolData}。
     */
    @Genericable(id = "modelengine.jade.store.tool.getTool.byVersion")
    StoreToolData getToolByVersion(String toolUniqueName, String version);

    /**
     * 查找数据库中已存在的定义名集合，用于判断重复。
     *
     * @param defGroupNames 表示指定的定义组名的 {@link String}{@code <}{@link String}{@code >}。
     * @return 表示第一个匹配的定义组名的 {@link List}{@code <}{@link String}{@code >}。
     */
    @Genericable(id = "modelengine.jade.store.tool.find.exist.groups")
    ListResult<DefinitionGroupData> findExistDefGroups(List<String> defGroupNames);
}
