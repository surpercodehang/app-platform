/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolSchema;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 提供一个返回去除默认参数摘要信息的元素信息结构。
 *
 * @author 王攀博
 * @since 2024-04-22
 */
public class DefaultValueFilterToolInfo implements Tool.Info {
    private final Tool.Info toolInfo;

    /**
     * 构造函数
     *
     * @param toolInfo 工具信息
     */
    public DefaultValueFilterToolInfo(Tool.Info toolInfo) {
        notNull(toolInfo, "the tool info cannot be null");
        this.toolInfo = toolInfo;
    }

    @Override
    public Map<String, Object> schema() {
        // 过滤掉摘要信息中拥有默认值的参数。
        Map<String, Object> schema = new HashMap<>(this.toolInfo.schema());
        return getFilterSchema(schema);
    }

    /**
     * 获取加工后的元数据信息
     *
     * @param schema 目标元数据
     * @return 加工后的元数据信息
     */
    public static Map<String, Object> getFilterSchema(Map<String, Object> schema) {
        Map<String, Object> parametersSchema = cast(schema.get(ToolSchema.PARAMETERS));
        if (MapUtils.isEmpty(parametersSchema)) {
            return schema;
        }
        Map<String, Object> properties = cast(parametersSchema.get(ToolSchema.PARAMETERS_PROPERTIES));
        if (MapUtils.isEmpty(properties)) {
            return schema;
        }
        filterDefaultParams(properties, parametersSchema);
        parametersSchema.remove(ToolSchema.PARAMETERS_ORDER);
        filterDynamicParams(properties, schema);
        schema.remove(ToolSchema.PARAMETERS_EXTENSIONS);
        return schema;
    }

    private static void filterDefaultParams(Map<String, Object> properties, Map<String, Object> parametersSchema) {
        List<String> defaultKeyList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Map<String, Object> property = ObjectUtils.getIfNull(cast(entry.getValue()), Collections::emptyMap);
            // 删除有默认值的参数
            if (property.get(ToolSchema.DEFAULT_PARAMETER) != null) {
                iterator.remove();
                defaultKeyList.add(entry.getKey());
            }
        }
        // 过滤 required 中拥有默认值的参数。
        List<String> requiredList = cast(parametersSchema.get(ToolSchema.PARAMETERS_REQUIRED));
        if (CollectionUtils.isEmpty(requiredList)) {
            return;
        }
        requiredList.removeIf(defaultKeyList::contains);
        parametersSchema.put(ToolSchema.PARAMETERS_REQUIRED, requiredList);
    }

    private static void filterDynamicParams(Map<String, Object> properties, Map<String, Object> schema) {
        getConfigParamName(schema).forEach(properties::remove);

        if (!properties.containsKey(WaterFlowToolConst.INPUT_PARAMS_KEY)) {
            return;
        }
        Map<String, Object> inputParams = cast(properties.get(WaterFlowToolConst.INPUT_PARAMS_KEY));
        Map<String, Object> inputParamsProperties = cast(inputParams.get(ToolSchema.PARAMETERS_PROPERTIES));
        inputParamsProperties.remove(WaterFlowToolConst.TRACE_ID);
        inputParamsProperties.remove(WaterFlowToolConst.CALLBACK_ID);
        inputParamsProperties.remove(WaterFlowToolConst.USER_ID);
    }

    private static List<String> getConfigParamName(Map<String, Object> schema) {
        Map<String, Object> extension = cast(schema.get(ToolSchema.PARAMETERS_EXTENSIONS));
        if (MapUtils.isEmpty(extension)) {
            return Collections.emptyList();
        }
        return cast(extension.getOrDefault(ToolSchema.CONFIG_PARAMETERS, Collections.emptyList()));
    }

    @Override
    public Map<String, Object> runnables() {
        return this.toolInfo.runnables();
    }

    @Override
    public String name() {
        return this.toolInfo.name();
    }

    @Override
    public String uniqueName() {
        return this.toolInfo.uniqueName();
    }

    @Override
    public String groupName() {
        return this.toolInfo.groupName();
    }

    @Override
    public String definitionName() {
        return this.toolInfo.definitionName();
    }

    @Override
    public String definitionGroupName() {
        return this.toolInfo.definitionGroupName();
    }

    @Override
    public String namespace() {
        return this.toolInfo.namespace();
    }

    @Override
    public String description() {
        return this.toolInfo.description();
    }

    @Override
    public Map<String, Object> parameters() {
        return this.toolInfo.parameters();
    }

    @Override
    public Map<String, Object> extensions() {
        return this.toolInfo.extensions();
    }

    @Override
    public String version() {
        return this.toolInfo.version();
    }

    @Override
    public boolean isLatest() {
        return this.toolInfo.isLatest();
    }

    @Override
    public String returnConverter() {
        return this.toolInfo.returnConverter();
    }

    @Override
    public Map<String, Object> defaultParameterValues() {
        return this.toolInfo.defaultParameterValues();
    }
}
