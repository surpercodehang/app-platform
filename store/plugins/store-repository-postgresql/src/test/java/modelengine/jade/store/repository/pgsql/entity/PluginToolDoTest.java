/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.repository.pgsql.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.jade.store.entity.transfer.PluginToolData;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

/**
 * 表示 {@link PluginToolDo} 的单元测试。
 *
 * @author 鲁为
 * @since 2024-08-09
 */
@DisplayName("测试 PluginToolDo")
public class PluginToolDoTest {
    @Test
    @DisplayName("从传输层实体类转换数据库类，返回正确结果")
    void shouldSuccessWhenFromPluginToolData() {
        PluginToolData pluginToolData = new PluginToolData();
        pluginToolData.setCreator("user");
        pluginToolData.setModifier("user");
        pluginToolData.setPluginId("testPluginId");
        PluginToolDo pluginToolDo = PluginToolDo.fromPluginToolData(pluginToolData);
        assertThat(pluginToolDo.getCreator()).isEqualTo("user");
        assertThat(pluginToolDo.getModifier()).isEqualTo("user");
        assertThat(pluginToolDo.getPluginId()).isEqualTo("testPluginId");
    }

    @Test
    @DisplayName("从数据库类转换传输层实体类，返回正确结果")
    void shouldSuccessWhenToPluginToolData() {
        PluginToolDo pluginToolDo = new PluginToolDo();
        pluginToolDo.setPluginId("testPluginId");
        ToolData toolData = new ToolData();
        PluginToolData pluginToolData = PluginToolDo.convertToPluginToolData(toolData, pluginToolDo, new HashSet<>());
        assertThat(pluginToolData.getPluginId()).isEqualTo("testPluginId");
    }
}
