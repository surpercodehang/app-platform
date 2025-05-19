/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository;

import modelengine.fel.tool.model.transfer.ToolGroupData;

import java.util.List;
import java.util.Optional;

/**
 * 表示工具组的仓库。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
public interface ToolGroupRepository {
    /**
     * 添加工具组。
     *
     * @param toolGroupData 表示工具组的 {@link ToolGroupData}。
     */
    void add(ToolGroupData toolGroupData);

    /**
     * 获取工具组。
     *
     * @param defGroupName 表示定义组的名称的 {@link String}。
     * @param name 表示工具组的名称的 {@link String}。
     * @return 表示工具组的 {@link Optional}{@link <}{@link ToolGroupData}{@link >}。
     */
    Optional<ToolGroupData> get(String defGroupName, String name);

    /**
     * 获取工具组集合。
     *
     * @param defGroupName 表示定义组的名称的 {@link String}。
     * @return 表示工具组集合的 {@link List}{@link <}{@link ToolGroupData}{@link >}。
     */
    List<ToolGroupData> getByDefGroupName(String defGroupName);

    /**
     * 删除工具组。
     *
     * @param defGroupName 表示定义组的名称的 {@link String}。
     * @param name 表示工具组的名称的 {@link String}。
     */
    void delete(String defGroupName, String name);

    /**
     * 删除工具组集合。
     *
     * @param defGroupName 表示定义组的名称的 {@link String}。
     */
    void deleteByDefGroupName(String defGroupName);
}
