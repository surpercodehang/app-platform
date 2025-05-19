/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.entity.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.tool.model.transfer.ToolData;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 表示 {@link StoreToolData} 的测试类。
 *
 * @author 李金绪
 * @since 2024-09-19
 */
public class StoreToolDataTest {
    @Test
    @DisplayName("测试转换")
    void shouldOkWhenfrom() {
        ToolData toolData = new ToolData();
        toolData.setUniqueName("uniqueName");
        Map<String, Object> schema = new HashMap<>();
        schema.put("name", "name");
        schema.put("description", "description");
        toolData.setSchema(schema);
        toolData.setRunnables(new HashMap<>());
        toolData.setExtensions(new HashMap<>());
        toolData.setVersion("version");
        toolData.setLatest(true);

        HashSet<String> tags = new HashSet<>(Arrays.asList("FIT"));
        StoreToolData storeToolData = StoreToolData.from(toolData, tags);
        assertThat(storeToolData.getTags()).isEqualTo(new HashSet<>(Arrays.asList("FIT")));

        storeToolData.setCreator("creator");
        storeToolData.setModifier("modifier");
        storeToolData.setSource("source");
        storeToolData.setIcon("icon");
        assertThat(storeToolData.getCreator()).isEqualTo("creator");
        assertThat(storeToolData.getModifier()).isEqualTo("modifier");
        assertThat(storeToolData.getSource()).isEqualTo("source");
        assertThat(storeToolData.getIcon()).isEqualTo("icon");
    }
}
