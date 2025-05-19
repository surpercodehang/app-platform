/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.model.transfer.DefinitionGroupData;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.jade.carver.tool.repository.pgsql.mapper.DefinitionGroupMapper;
import modelengine.jade.carver.tool.repository.pgsql.model.entity.DefinitionGroupDo;
import modelengine.jade.carver.tool.repository.pgsql.repository.DefinitionGroupRepository;

import java.util.Optional;

/**
 * 存入数据库的定义组仓库。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
@Component
public class DefaultDefinitionGroupRepository implements DefinitionGroupRepository {
    private final DefinitionGroupMapper defGroupMapper;
    private final ObjectSerializer serializer;

    /**
     * 构造函数。
     *
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @param defGroupMapper 标识操作工具组的 mapper 接口的 {@link DefinitionGroupMapper}。
     */
    public DefaultDefinitionGroupRepository(@Fit(alias = "json") ObjectSerializer serializer,
            DefinitionGroupMapper defGroupMapper) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.defGroupMapper = notNull(defGroupMapper, "The definition group mapper cannot be null.");
    }

    @Override
    public void add(DefinitionGroupData groupData) {
        DefinitionGroupDo defGroupDo = DefinitionGroupDo.data2Do(groupData, serializer);
        this.defGroupMapper.add(defGroupDo);
    }

    @Override
    public Optional<DefinitionGroupData> get(String name) {
        DefinitionGroupDo defGroupDo = this.defGroupMapper.get(name);
        if (defGroupDo == null) {
            return Optional.empty();
        }
        return Optional.of(DefinitionGroupDo.do2Data(defGroupDo, serializer));
    }

    @Override
    public void delete(String name) {
        this.defGroupMapper.delete(name);
    }
}
