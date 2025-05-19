/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import modelengine.fel.tool.Tool;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * 表示 {@link WaterFlowTool} 的单元测试。
 *
 * @author 王攀博
 * @since 2024-04-22
 */
@DisplayName("测试 DefaultValueFilterItemInfo")
public class DefaultValueFilterToolInfoTest {
    private Tool.Info fitToolInfo;
    private Tool.Info waterFlowToolInfo;

    @BeforeEach
    void setup() throws JsonProcessingException {
        this.fitToolInfo = new DefaultValueFilterToolInfo(this.buildToolInfo(this.buildFitToolSchema()));
        this.waterFlowToolInfo = new DefaultValueFilterToolInfo(this.buildToolInfo(this.buildWaterFlowToolSchema()));
    }

    private Tool.Info buildToolInfo(Map<String, Object> schema) {
        return Tool.Info.custom()
                .name("test_schema_default_implementation_name")
                .uniqueName("decorator-customize-tool-uuid")
                .schema(schema)
                .build();
    }

    private Map<String, Object> buildFitToolSchema() {
        return MapBuilder.<String, Object>get()
                .put("name", "test_schema_default_implementation_name")
                .put("description", "This is a demo schema fit tool.")
                .put("parameters",
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put("properties",
                                        MapBuilder.<String, Object>get()
                                                .put("p1",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "string")
                                                                .put("description", "This is the first parameter.")
                                                                .build())
                                                .put("p2",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "string")
                                                                .put("default", "p2_default_value.")
                                                                .build())
                                                .put("p3",
                                                        MapBuilder.<String, Object>get().put("type", "string").build())
                                                .build())
                                .put("order", new ArrayList<>(Arrays.asList("p1", "p2", "p3")))
                                .put("required", new ArrayList<>(Arrays.asList("p1", "p2", "p3")))
                                .build())
                .put("return", MapBuilder.<String, Object>get().put("type", "string").build())
                .build();
    }

    private Map<String, Object> buildWaterFlowToolSchema() throws JsonProcessingException {
        String waterFlowSchema =
                "{\"name\":\"test_schema_default_implementation_name\",\"description\":\"This is a demo schema "
                        + "waterflow tool.\",\"parameters\":{\"type\":\"object\","
                        + "\"properties\":{\"inputParams\":{\"type\":\"object\","
                        + "\"properties\":{\"traceId\":{\"type\":\"string\"},\"callbackId\":{\"type\":\"string\"},"
                        + "\"query\":{\"type\":\"string\",\"description\":\"用户问题。\"}}}},"
                        + "\"required\":[\"inputParams\"],\"order\":[\"inputParams\"]},"
                        + "\"return\":{\"type\":\"string\"}}";
        return new ObjectMapper().readValue(waterFlowSchema, new TypeReference<Map<String, Object>>() {});
    }

    @Test
    @DisplayName("过滤掉默认参数")
    void shouldReturnCorrectResultWhenFilterDefaultParams() {
        Map<String, Object> schema = this.fitToolInfo.schema();
        assertThat(schema).containsEntry("name", "test_schema_default_implementation_name")
                .containsEntry("description", "This is a demo schema fit tool.")
                .containsEntry("parameters",
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put("properties",
                                        MapBuilder.<String, Object>get()
                                                .put("p1",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "string")
                                                                .put("description", "This is the first parameter.")
                                                                .build())
                                                .put("p3",
                                                        MapBuilder.<String, Object>get().put("type", "string").build())
                                                .build())
                                .put("required", new ArrayList<>(Arrays.asList("p1", "p3")))
                                .build())
                .containsEntry("return", MapBuilder.<String, Object>get().put("type", "string").build());
    }

    @Test
    @DisplayName("过滤掉动态参数")
    void shouldReturnCorrectResultWhenFilterDynamicParams() {
        Map<String, Object> schema = this.waterFlowToolInfo.schema();
        assertThat(schema).containsEntry("name", "test_schema_default_implementation_name")
                .containsEntry("description", "This is a demo schema waterflow tool.")
                .containsEntry("parameters",
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put("properties",
                                        MapBuilder.<String, Object>get()
                                                .put("inputParams",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "object")
                                                                .put("properties",
                                                                        MapBuilder.<String, Object>get()
                                                                                .put("query",
                                                                                        MapBuilder.<String, Object>get()
                                                                                                .put("type", "string")
                                                                                                .put("description",
                                                                                                        "用户问题。")
                                                                                                .build())
                                                                                .build())
                                                                .build())
                                                .build())
                                .put("required", new ArrayList<>(Collections.singletonList("inputParams")))
                                .build())
                .containsEntry("return", MapBuilder.<String, Object>get().put("type", "string").build());
    }
}
