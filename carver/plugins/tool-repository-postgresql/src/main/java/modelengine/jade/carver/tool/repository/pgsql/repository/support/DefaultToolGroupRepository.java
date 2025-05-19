/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.model.transfer.ToolGroupData;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.jade.carver.tool.repository.pgsql.mapper.ToolGroupMapper;
import modelengine.jade.carver.tool.repository.pgsql.model.entity.ToolGroupDo;
import modelengine.jade.carver.tool.repository.pgsql.repository.ToolGroupRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 存入数据库的实现组仓库。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
@Component
public class DefaultToolGroupRepository implements ToolGroupRepository {
    private final ObjectSerializer serializer;
    private final ToolGroupMapper toolGroupMapper;

    /**
     * 构造函数。
     *
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @param toolGroupMapper 标识操作工具组的 mapper 接口的 {@link ToolGroupMapper}。
     */
    public DefaultToolGroupRepository(@Fit(alias = "json") ObjectSerializer serializer,
            ToolGroupMapper toolGroupMapper) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.toolGroupMapper = notNull(toolGroupMapper, "The tool group mapper cannot be null.");
    }

    @Override
    public void add(ToolGroupData toolGroupData) {
        ToolGroupDo toolGroupDo = ToolGroupDo.data2Do(toolGroupData, serializer);
        this.toolGroupMapper.add(toolGroupDo);
    }

    @Override
    public Optional<ToolGroupData> get(String defGroupName, String name) {
        ToolGroupDo toolGroupDo = this.toolGroupMapper.get(defGroupName, name);
        if (toolGroupDo == null) {
            return Optional.empty();
        }
        return Optional.of(ToolGroupDo.do2Data(toolGroupDo, serializer));
    }

    @Override
    public List<ToolGroupData> getByDefGroupName(String defGroupName) {
        List<ToolGroupDo> toolGroupDos = this.toolGroupMapper.getByDefGroupName(defGroupName);
        return toolGroupDos.stream()
                .map(toolGroupDo -> ToolGroupDo.do2Data(toolGroupDo, serializer))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String defGroupName, String name) {
        this.toolGroupMapper.delete(defGroupName, name);
    }

    @Override
    public void deleteByDefGroupName(String defGroupName) {
        this.toolGroupMapper.deleteByDefGroupName(defGroupName);
    }
}
