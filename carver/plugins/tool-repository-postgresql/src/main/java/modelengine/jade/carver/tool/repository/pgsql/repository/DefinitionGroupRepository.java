/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository;

import modelengine.fel.tool.model.transfer.DefinitionGroupData;

import java.util.Optional;

/**
 * 表示定义组的仓库。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
public interface DefinitionGroupRepository {
    /**
     * 添加定义组。
     *
     * @param defGroupData 表示定义组的 {@link DefinitionGroupData}。
     */
    void add(DefinitionGroupData defGroupData);

    /**
     * 获取定义组。
     *
     * @param name 表示定义组的名称的 {@link String}。
     * @return 表示定义组的 {@link Optional}{@link <}{@link DefinitionGroupData}{@link >}。
     */
    Optional<DefinitionGroupData> get(String name);

    /**
     * 删除定义组。
     *
     * @param name 表示定义组的名称的 {@link String}。
     */
    void delete(String name);
}
