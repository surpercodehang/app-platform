/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.Tool;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.transaction.Transactional;
import modelengine.jade.carver.tool.repository.pgsql.mapper.DefinitionMapper;
import modelengine.jade.carver.tool.repository.pgsql.model.entity.DefinitionDo;
import modelengine.jade.carver.tool.repository.pgsql.repository.DefinitionRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 存入数据库的定义仓库。
 *
 * @author 王攀博
 * @since 2024-10-29
 */
@Component
public class DefaultDefinitionRepository implements DefinitionRepository {
    private static final Logger log = Logger.get(DefaultDefinitionRepository.class);

    private final ObjectSerializer serializer;
    private final DefinitionMapper definitionMapper;

    public DefaultDefinitionRepository(@Fit(alias = "json") ObjectSerializer serializer,
            DefinitionMapper definitionMapper) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.definitionMapper = notNull(definitionMapper, "The definition mapper cannot be null.");
    }

    @Override
    @Transactional
    public String add(Tool.Metadata metadata) {
        DefinitionDo definitionDo = DefinitionDo.info2Do(metadata, this.serializer);
        return this.definitionMapper.add(definitionDo).toString();
    }

    @Override
    @Transactional
    public void add(List<Tool.Metadata> metadataList) {
        this.definitionMapper.addDefinitions(metadataList.stream()
                .map(metadata -> DefinitionDo.info2Do(metadata, this.serializer))
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void delete(String groupName, String name) {
        this.definitionMapper.deleteByName(groupName, name);
    }

    @Override
    @Transactional
    public void delete(String groupName) {
        this.definitionMapper.deleteByGroup(groupName);
    }

    @Override
    public Tool.Metadata get(String groupName, String name) {
        DefinitionDo definitionDo = this.definitionMapper.getByName(groupName, name);
        if (definitionDo == null) {
            return null;
        }
        return DefinitionDo.do2Info(definitionDo, this.serializer);
    }

    @Override
    public List<Tool.Metadata> get(String groupName) {
        return this.definitionMapper.getByGroup(groupName)
                .stream()
                .map(definitionDo -> DefinitionDo.do2Info(definitionDo, this.serializer))
                .collect(Collectors.toList());
    }
}
