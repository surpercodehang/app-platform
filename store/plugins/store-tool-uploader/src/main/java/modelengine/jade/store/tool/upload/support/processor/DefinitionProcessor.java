/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.upload.support.processor;

import static modelengine.fel.tool.ToolSchema.NAME;
import static modelengine.fel.tool.info.schema.PluginSchema.DOT;
import static modelengine.fel.tool.info.schema.ToolsSchema.DEFINITIONS;
import static modelengine.fel.tool.info.schema.ToolsSchema.DEFINITION_GROUPS;
import static modelengine.fel.tool.info.schema.ToolsSchema.LIST_NOTATION;
import static modelengine.fel.tool.info.schema.ToolsSchema.TOOLS_JSON;
import static modelengine.fitframework.inspection.Validation.notEmpty;
import static modelengine.fitframework.util.ObjectUtils.cast;
import static modelengine.jade.store.tool.upload.support.BasicValidator.buildEmptyParserException;
import static modelengine.jade.store.tool.upload.utils.FormatFileUtils.objToMap;

import modelengine.fel.tool.info.entity.DefinitionEntity;
import modelengine.fel.tool.info.entity.DefinitionGroupEntity;
import modelengine.fel.tool.info.entity.ToolJsonEntity;
import modelengine.fel.tool.model.transfer.DefinitionData;
import modelengine.fel.tool.model.transfer.DefinitionGroupData;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定义信息的处理器。
 *
 * @author 李金绪
 * @since 2024-10-29
 */
@Component
public class DefinitionProcessor extends Processor {
    private final ObjectSerializer serializer;

    /**
     * 用于构造一个 {@link PluginProcessor} 的新实例。
     *
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     */
    public DefinitionProcessor(@Fit(alias = "json") ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void validate(Object data, Map<String, Object> helper) {
        ToolJsonEntity entity = cast(data);
        List<DefinitionGroupEntity> defGroups = entity.getDefinitionGroups();
        notEmpty(defGroups, () -> buildEmptyParserException(TOOLS_JSON, DEFINITION_GROUPS));
        for (DefinitionGroupEntity defGroup : defGroups) {
            this.validateName(defGroup.getName(), TOOLS_JSON, DEFINITION_GROUPS + LIST_NOTATION + DOT + NAME);
            List<DefinitionEntity> defs = defGroup.getDefinitions();
            notEmpty(defs,
                    () -> buildEmptyParserException(TOOLS_JSON, DEFINITION_GROUPS + LIST_NOTATION + DOT + DEFINITIONS));
            for (DefinitionEntity def : defs) {
                this.validateSchemaStrictly(TOOLS_JSON, def.getSchema());
            }
        }
    }

    @Override
    public Object transform(Object data, Map<String, Object> helper) {
        ToolJsonEntity entity = cast(data);
        return entity.getDefinitionGroups().stream().map(this::buildDefGroupData).collect(Collectors.toList());
    }

    private DefinitionGroupData buildDefGroupData(DefinitionGroupEntity defGroupEntity) {
        DefinitionGroupData definitionGroupData = new DefinitionGroupData();
        String defGroupName = defGroupEntity.getName();
        definitionGroupData.setName(defGroupName);
        definitionGroupData.setSummary(defGroupEntity.getSummary() != null ? defGroupEntity.getSummary() : null);
        definitionGroupData.setDescription(
                defGroupEntity.getDescription() != null ? defGroupEntity.getDescription() : null);
        definitionGroupData.setExtensions(
                defGroupEntity.getExtensions() != null ? defGroupEntity.getExtensions() : null);
        definitionGroupData.setDefinitions(defGroupEntity.getDefinitions()
                .stream()
                .map(defEntity -> this.buildDefData(defEntity, defGroupName))
                .collect(Collectors.toList()));
        return definitionGroupData;
    }

    private DefinitionData buildDefData(DefinitionEntity defEntity, String defGroupName) {
        DefinitionData defData = new DefinitionData();
        defData.setName(defEntity.getSchema().getName());
        defData.setDescription(defEntity.getSchema().getDescription());
        defData.setGroupName(defGroupName);
        defData.setSchema(objToMap(this.serializer, defEntity.getSchema()));
        return defData;
    }
}
