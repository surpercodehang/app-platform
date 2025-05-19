/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql;

import modelengine.fel.tool.ToolSchema;
import modelengine.fel.tool.model.transfer.DefinitionData;
import modelengine.fel.tool.model.transfer.DefinitionGroupData;
import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fel.tool.model.transfer.ToolGroupData;

import modelengine.fitframework.util.MapBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具测试数据的构建者。
 *
 * @author 王攀博
 * @since 2024-11-05
 */
public class ToolDataBuilder {
    /**
     * 构建工具数据。
     *
     * @return 表示工具数据的 {@link ToolData}。
     */
    public static ToolData mockToolData() {
        ToolData toolData = new ToolData();
        toolData.setUniqueName("uniqueName");
        toolData.setName("name");
        toolData.setGroupName("groupName");
        toolData.setDefName("defName");
        toolData.setDefGroupName("defGroupName");
        Map<String, Object> schema = new HashMap<String, Object>();
        schema.put("namespace", "namespace");
        schema.put("name", "name");
        schema.put("description", "description");
        schema.put("parameters", new HashMap<>());
        toolData.setSchema(schema);
        toolData.setRunnables(new HashMap<String, Object>());
        toolData.setExtensions(new HashMap<String, Object>());
        toolData.setVersion("version");
        toolData.setLatest(true);
        return toolData;
    }

    /**
     * 构建定义数据。
     *
     * @return 表示定义数据的 {@link DefinitionData}。
     */
    public static DefinitionData mockDefinitionData() {
        DefinitionData definitionData = new DefinitionData();
        definitionData.setName("defName");
        definitionData.setGroupName("defGroupName");
        definitionData.setDescription("This is a demo definition schema");
        Map<String, Object> schema = ToolDataBuilder.buildSchema();
        definitionData.setSchema(schema);
        return definitionData;
    }

    /**
     * 构建工具摘要信息。
     *
     * @return 表示构建工具摘要信息的 {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}。
     */
    public static Map<String, Object> buildSchema() {
        return MapBuilder.<String, Object>get()
                .put("name", "defName")
                .put("description", "This is a demo definition schema.")
                .put(ToolSchema.PARAMETERS,
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put(ToolSchema.PARAMETERS_PROPERTIES,
                                        MapBuilder.<String, Object>get()
                                                .put("pt",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "string")
                                                                .put("default", "The first parameter is string.")
                                                                .build())
                                                .build())
                                .build())
                .put(ToolSchema.PARAMETERS_ORDER, Collections.singletonList("pt"))
                .put(ToolSchema.RETURN_SCHEMA, MapBuilder.<String, Object>get().put("type", "string").build())
                .build();
    }

    /**
     * 表示构建定义组数据。
     *
     * @return 表示构建的定义组数据的 {@link DefinitionGroupData}。
     */
    public static DefinitionGroupData mockDefinitionGroupData() {
        DefinitionData definitionData = new DefinitionData();
        definitionData.setName("defName");
        Map<String, Object> schema = ToolDataBuilder.buildSchema();
        definitionData.setSchema(schema);
        definitionData.setGroupName("defGroupName");

        DefinitionGroupData groupData = new DefinitionGroupData();
        groupData.setName("defGroupName");
        groupData.setDefinitions(Collections.singletonList(definitionData));
        groupData.setSummary("summary");
        groupData.setDescription("description");
        groupData.setExtensions(schema);
        return groupData;
    }

    /**
     * 表示构建的测试用的工具组数据。
     *
     * @return 返回测试用的工具组数据的 {@link ToolGroupData}。
     */
    public static ToolGroupData mockToolGroupData() {
        ToolData toolData = new ToolData();
        toolData.setUniqueName("uniqueName");
        toolData.setName("name");
        toolData.setGroupName("groupName");
        toolData.setDefName("defName");
        toolData.setDefGroupName("defGroupName");
        Map<String, Object> schema = new HashMap<>();
        schema.put("namespace", "namespace");
        schema.put("name", "name");
        schema.put("description", "description");
        schema.put("parameters", new HashMap<>());
        toolData.setSchema(schema);
        toolData.setRunnables(new HashMap<>());
        toolData.setExtensions(new HashMap<>());
        toolData.setVersion("version");
        toolData.setLatest(true);

        ToolGroupData toolGroupData = new ToolGroupData();
        toolGroupData.setTools(Collections.singletonList(toolData));
        toolGroupData.setName("groupName");
        toolGroupData.setDefGroupName("defGroupName");
        toolGroupData.setSummary("summary");
        toolGroupData.setDescription("description");
        toolGroupData.setExtensions(schema);
        toolGroupData.setDefGroupName("defGroupName");
        return toolGroupData;
    }
}