/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.repository.pgsql.mapper;

import modelengine.fel.tool.model.entity.ToolIdentifier;
import modelengine.jade.store.entity.query.ToolQuery;

import java.util.List;

/**
 * 表示用于 MyBatis 持久层引用的 StoreTool 接口。
 *
 * @author 李金绪
 * @since 2024-09-13
 */
public interface StoreToolMapper {
    /**
     * 根据动态条件准确获取所有的工具。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 所有工具标识列表 {@link List}{@code <}{@link ToolIdentifier}{@code >}。
     */
    List<ToolIdentifier> getTools(ToolQuery toolQuery);

    /**
     * 根据动态条件模糊获取所有的工具。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 所有工具标识列表 {@link List}{@code <}{@link ToolIdentifier}{@code >}。
     */
    List<ToolIdentifier> searchTools(ToolQuery toolQuery);

    /**
     * 根据动态条件模糊获取工具总数。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示工具总数的 {@code int}。
     */
    int getToolsCount(ToolQuery toolQuery);

    /**
     * 根据动态条件模糊获取所有的工具。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示工具总数的 {@code int}。
     */
    int searchToolsCount(ToolQuery toolQuery);
}
