/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.tool.upload.support.processor;

import static modelengine.fel.tool.ToolSchema.DESCRIPTION;
import static modelengine.fel.tool.ToolSchema.EXTENSIONS;
import static modelengine.fel.tool.ToolSchema.NAME;
import static modelengine.fel.tool.ToolSchema.RUNNABLE;
import static modelengine.fel.tool.ToolSchema.SCHEMA;
import static modelengine.fel.tool.info.schema.PluginSchema.DOT;
import static modelengine.fel.tool.info.schema.ToolsSchema.DEFINITION_GROUP_NAME_IN_TOOL;
import static modelengine.fel.tool.info.schema.ToolsSchema.DEFINITION_NAME;
import static modelengine.fel.tool.info.schema.ToolsSchema.FIT;
import static modelengine.fel.tool.info.schema.ToolsSchema.FITABLE_ID;
import static modelengine.fel.tool.info.schema.ToolsSchema.GENERICABLE_ID;
import static modelengine.fel.tool.info.schema.ToolsSchema.LIST_NOTATION;
import static modelengine.fel.tool.info.schema.ToolsSchema.MAX_TAG_LENGTH;
import static modelengine.fel.tool.info.schema.ToolsSchema.TAGS;
import static modelengine.fel.tool.info.schema.ToolsSchema.TOOLS;
import static modelengine.fel.tool.info.schema.ToolsSchema.TOOLS_JSON;
import static modelengine.fel.tool.info.schema.ToolsSchema.TOOL_GROUPS;
import static modelengine.fitframework.inspection.Validation.notEmpty;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;
import static modelengine.jade.store.tool.upload.support.BasicValidator.buildEmptyParserException;
import static modelengine.jade.store.tool.upload.support.BasicValidator.buildNullParserException;
import static modelengine.jade.store.tool.upload.support.BasicValidator.buildParserException;
import static modelengine.jade.store.tool.upload.utils.FormatFileUtils.buildDefGroupMap;
import static modelengine.jade.store.tool.upload.utils.FormatFileUtils.objToMap;

import modelengine.fel.tool.info.entity.DefinitionEntity;
import modelengine.fel.tool.info.entity.ToolEntity;
import modelengine.fel.tool.info.entity.ToolGroupEntity;
import modelengine.fel.tool.info.entity.ToolJsonEntity;
import modelengine.fel.tool.model.transfer.DefinitionData;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fel.tool.model.transfer.ToolGroupData;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.merge.ConflictResolverCollection;
import modelengine.fitframework.merge.list.ListRemoveDuplicationConflictResolver;
import modelengine.fitframework.merge.map.MapConflictResolver;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.jade.common.exception.ModelEngineException;
import modelengine.jade.store.code.PluginRetCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工具信息的处理器。
 *
 * @author 李金绪
 * @since 2024-10-29
 */
@Component
public class ToolProcessor extends Processor {
    /** fitable、genericable 仅支持数字、大小写字母以及 '-'、'_'、'*'、'.' 字符且长度在128以内。 */
    private static final Pattern ID_PATTERN = Pattern.compile("^[\\w\\-\\.\\*]{1,128}+$");
    private static final String DEF_GROUP_MAP = "defGroupMap";
    private static final String TOOL_GROUPS_TOOLS_FORMAT = TOOL_GROUPS + LIST_NOTATION + DOT + TOOLS + LIST_NOTATION;

    private final ObjectSerializer serializer;

    /**
     * 用于构造一个 {@link PluginProcessor} 的新实例。
     *
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     */
    public ToolProcessor(@Fit(alias = "json") ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void validate(Object data, Map<String, Object> helper) {
        ToolJsonEntity entity = cast(data);
        List<ToolGroupEntity> toolGroups = entity.getToolGroups();
        notEmpty(toolGroups, () -> buildEmptyParserException(TOOLS_JSON, TOOL_GROUPS));
        List<String> fitIdentifiers = new ArrayList<>();
        for (ToolGroupEntity toolGroup : toolGroups) {
            this.validateName(toolGroup.getName(), TOOLS_JSON, TOOL_GROUPS + LIST_NOTATION + DOT + NAME);
            this.validateName(toolGroup.getDefinitionGroupName(),
                    TOOLS_JSON,
                    TOOL_GROUPS + LIST_NOTATION + DOT + DEFINITION_GROUP_NAME_IN_TOOL);
            List<ToolEntity> tools = toolGroup.getTools();
            notEmpty(tools, () -> buildEmptyParserException(TOOLS_JSON, TOOL_GROUPS + LIST_NOTATION + DOT + TOOLS));
            fitIdentifiers.addAll(this.validateTools(cast(tools)));
            this.validateRepeatFitValue(fitIdentifiers);
        }
    }

    private List<String> validateTools(List<Object> tools) {
        List<String> fitIdentifiers = new ArrayList<>();
        for (Object tool : tools) {
            ToolEntity toolEntity = cast(tool);
            this.validateExtensions(toolEntity);
            String fitIdentifier = this.validateRunables(toolEntity.getRunnables());
            if (!StringUtils.isEmpty(fitIdentifier)) {
                fitIdentifiers.add(fitIdentifier);
            }
            this.validateCompatibility(tool);
        }
        return fitIdentifiers;
    }

    private void validateCompatibility(Object tool) {
        ToolEntity toolFour = cast(tool);
        this.validateName(toolFour.getDefinitionName(), TOOLS_JSON, TOOL_GROUPS_TOOLS_FORMAT + DOT + DEFINITION_NAME);
    }

    private void validateExtensions(ToolEntity tool) {
        Map<String, Object> extensions = tool.getExtensions();
        String extensionsFormat = TOOL_GROUPS_TOOLS_FORMAT + DOT + EXTENSIONS;
        if (MapUtils.isEmpty(extensions)) {
            throw buildEmptyParserException(TOOLS_JSON, extensionsFormat);
        }
        List<String> extensionTags = cast(extensions.get(TAGS));
        notNull(extensionTags, () -> buildNullParserException(TOOLS_JSON, extensionsFormat + DOT + TAGS));
        this.validateTags(extensionTags);
    }

    private void validateTags(List<String> tags) {
        notEmpty(tags,
                () -> buildEmptyParserException(TOOLS_JSON, TOOL_GROUPS_TOOLS_FORMAT + DOT + EXTENSIONS + DOT + TAGS));
        tags.stream().filter(tag -> tag.length() > MAX_TAG_LENGTH).findAny().ifPresent(tag -> {
            throw new ModelEngineException(PluginRetCode.LENGTH_EXCEEDED_LIMIT_FIELD, tag);
        });
    }

    private String validateRunables(Map<String, Object> runnables) {
        notNull(runnables, () -> buildNullParserException(TOOLS_JSON, TOOL_GROUPS_TOOLS_FORMAT + DOT + RUNNABLE));
        if (!runnables.containsKey(FIT)) {
            return StringUtils.EMPTY;
        }
        Map<String, Object> fit = cast(runnables.get(FIT));
        if (!fit.containsKey(FITABLE_ID)) {
            throw buildNullParserException(TOOLS_JSON, SCHEMA + DOT + RUNNABLE + DOT + FITABLE_ID);
        }
        this.validateFit(fit, FITABLE_ID);
        if (!fit.containsKey(GENERICABLE_ID)) {
            throw buildNullParserException(TOOLS_JSON, SCHEMA + DOT + RUNNABLE + DOT + GENERICABLE_ID);
        }
        this.validateFit(fit, GENERICABLE_ID);
        return cast(fit.get(FITABLE_ID)) + " + " + cast(fit.get(GENERICABLE_ID));
    }

    private void validateFit(Map<String, Object> fit, String fitKey) {
        Object fitValue = fit.get(fitKey);
        if (!(fitValue instanceof String)) {
            String message =
                    StringUtils.format("The type of field value in 'runnables' must be String. [field='{0}']", fitKey);
            throw buildParserException(message);
        }
        String fitValueStr = cast(fitValue);
        if (!ID_PATTERN.matcher(cast(fitValueStr)).matches()) {
            throw buildParserException(StringUtils.format(
                    "The fitable id or genericable id does not meet the naming requirements. [key='{0}', value='{1}']",
                    fitKey,
                    fitValueStr));
        }
    }

    private void validateRepeatFitValue(List<String> fitIdentifiers) {
        Set<String> fitIdSet = new HashSet<>();
        for (String fitId : fitIdentifiers) {
            if (!fitIdSet.add(fitId)) {
                throw buildParserException(StringUtils.format(
                        "The current operation has duplicate fitable id and genericable id. [fitableId='{0}']",
                        fitId));
            }
        }
    }

    @Override
    public Object transform(Object data, Map<String, Object> helper) {
        ToolJsonEntity entity = cast(data);
        helper.put(DEF_GROUP_MAP, buildDefGroupMap(entity));
        return entity.getToolGroups()
                .stream()
                .map(toolGroupEntity -> this.buildToolGroupData(toolGroupEntity, helper))
                .collect(Collectors.toList());
    }

    private ToolGroupData buildToolGroupData(ToolGroupEntity toolGroupEntity, Map<String, Object> helper) {
        ToolGroupData toolGroupData = new ToolGroupData();
        String defGroupName = toolGroupEntity.getDefinitionGroupName();
        String implGroupName = toolGroupEntity.getName();
        toolGroupData.setTools(toolGroupEntity.getTools()
                .stream()
                .map(toolEntity -> this.buildToolData(toolEntity, defGroupName, implGroupName, helper))
                .collect(Collectors.toList()));
        toolGroupData.setDefGroupName(defGroupName);
        toolGroupData.setName(implGroupName);
        toolGroupData.setSummary(toolGroupEntity.getSummary() != null ? toolGroupEntity.getSummary() : null);
        toolGroupData.setDescription(
                toolGroupEntity.getDescription() != null ? toolGroupEntity.getDescription() : null);
        toolGroupData.setExtensions(toolGroupEntity.getExtensions() != null ? toolGroupEntity.getExtensions() : null);
        return toolGroupData;
    }

    private ToolData buildToolData(ToolEntity toolEntity, String defGroupName, String implGroupName,
            Map<String, Object> helper) {
        Map<String, Map<String, Object>> defGroupMap = cast(helper.get(DEF_GROUP_MAP));
        ToolData toolData = this.buildBasicToolData(toolEntity);
        toolData.setDefGroupName(defGroupName);
        toolData.setGroupName(implGroupName);
        toolData.setDefName(toolEntity.getDefinitionName());
        if (toolData.getName() == null) {
            DefinitionEntity defEntity = cast(defGroupMap.get(toolData.getDefGroupName()).get(toolData.getDefName()));
            toolData.setName(defEntity.getSchema().getName());
        }
        if (toolData.getDescription() == null) {
            DefinitionEntity defEntity = cast(defGroupMap.get(toolData.getDefGroupName()).get(toolData.getDefName()));
            toolData.setDescription(defEntity.getSchema().getDescription());
        }
        if (toolData.getSchema() == null) {
            Map<String, Object> schema = new HashMap<>();
            schema.put(NAME, toolData.getName());
            schema.put(DESCRIPTION, toolData.getDescription());
            toolData.setSchema(schema);
        }
        return toolData;
    }

    private ToolData buildBasicToolData(ToolEntity toolEntity) {
        ToolData toolData = new ToolData();
        toolData.setName(toolEntity.getSchema() == null || toolEntity.getSchema().getName() == null
                ? null
                : toolEntity.getSchema().getName());
        toolData.setDescription(toolEntity.getSchema() == null || toolEntity.getSchema().getDescription() == null
                ? null
                : toolEntity.getSchema().getDescription());
        toolData.setUniqueName(UUID.randomUUID().toString());
        toolData.setSchema(objToMap(serializer, toolEntity.getSchema()));
        toolData.setRunnables(toolEntity.getRunnables());
        toolData.setExtensions(toolEntity.getExtensions());
        return toolData;
    }

    /**
     * 使用定义来组装工具的 Schema。
     *
     * @param toolGroupDatas 表示工具组数据的 {@link List<ToolGroupData>}。
     * @param defGroupMap 表示定义组映射的
     * {@link Map}{@code <}{@link String}{@code , }{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >>}。
     */
    public static void enhanceSchema(List<ToolGroupData> toolGroupDatas, Map<String, Map<String, Object>> defGroupMap) {
        // 已做校验，保证必须有各字段。
        ConflictResolverCollection registry = ConflictResolverCollection.createBasicOverwriteCollection();
        registry.add(Map.class, ObjectUtils.cast(new MapConflictResolver<>()));
        registry.add(List.class, ObjectUtils.cast(new ListRemoveDuplicationConflictResolver<>()));
        toolGroupDatas.forEach(toolGroupData -> toolGroupData.getTools().forEach(toolData -> {
            DefinitionData defData = cast(defGroupMap.get(toolData.getDefGroupName()).get(toolData.getDefName()));
            toolData.setSchema(MapUtils.merge(defData.getSchema(), toolData.getSchema(), registry));
        }));
    }
}