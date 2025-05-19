/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.repository.pgsql.repository;

import modelengine.fel.tool.model.entity.ToolIdentifier;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.jade.store.entity.query.ToolQuery;

import java.util.List;

/**
 * 包含额外信息的工具的仓库。
 *
 * @author 李金绪
 * @since 2024-09-13
 */
public interface StoreToolRepository {
    /**
     * 根据动态条件准确查询所有的工具信息的唯一标识。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示所有工具详细信息的唯一标识与版本的列表的 {@link List}{@code <}{@link ToolIdentifier>}{@code >}。
     */
    List<ToolIdentifier> getTools(ToolQuery toolQuery);

    /**
     * 根据动态条件准确查询所有的工具信息的唯一标识。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示所有工具详细信息的的唯一标识与版本的列表的 {@link List}{@code <}{@link ToolIdentifier>}{@code >}。
     */
    List<ToolIdentifier> searchTools(ToolQuery toolQuery);

    /**
     * 根据动态条件准确查询所有的工具信息。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示所有工具详细信息的列表的 {@link List}{@code <}{@link ToolData>}{@code >}。
     */
    int getToolsCount(ToolQuery toolQuery);

    /**
     * 根据动态条件准确查询所有的工具信息。
     *
     * @param toolQuery 表示动态查询条件的 {@link ToolQuery}。
     * @return 表示所有工具详细信息的列表的 {@link List}{@code <}{@link ToolData>}{@code >}。
     */
    int searchToolsCount(ToolQuery toolQuery);
}
