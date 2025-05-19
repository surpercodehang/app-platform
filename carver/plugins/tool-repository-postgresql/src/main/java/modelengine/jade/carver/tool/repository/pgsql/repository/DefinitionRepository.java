/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository;

import modelengine.fel.tool.Tool;

import java.util.List;

/**
 * 表示定义的仓库。
 *
 * @author 王攀博
 * @since 2024-10-29
 */
public interface DefinitionRepository {
    /**
     * 添加定义信息。
     *
     * @param metadata 表示待增加的定义信息的 {@link Tool.Metadata}。
     * @return 示添加后的定义的唯一标识的 {@link String}。
     */
    String add(Tool.Metadata metadata);

    /**
     * 添加定义信息列表。
     *
     * @param metadataList 表示待增加的定义信息的 {@link List}{@code <}{@link Tool.Metadata}{@code >}。
     */
    void add(List<Tool.Metadata> metadataList);

    /**
     * 删除指定名称的定义
     *
     * @param groupName 表示待删除定义组名的 {@link String}。
     * @param name 表示待删除定义名的 {@link String}。
     */
    void delete(String groupName, String name);

    /**
     * 删除指定义组下所有的的定义
     *
     * @param groupName 表示待删除定义组名的 {@link String}。
     */
    void delete(String groupName);

    /**
     * 获取定义信息。
     *
     * @param groupName 表示定义组名称的 {@link String}。
     * @param name 表示工具定义名称的 {@link String}。
     * @return 表示定义信息的 {@link Tool.Metadata}。
     */
    Tool.Metadata get(String groupName, String name);

    /**
     * 获取定义组下所有的定义信息。
     *
     * @param groupName 表示定义组名称的 {@link String}。
     * @return 表示定义信息的 {@link Tool.Metadata}。
     */
    List<Tool.Metadata> get(String groupName);
}
