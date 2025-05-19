/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.waterflow;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolFactory;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示自定义流式工具的工厂实现。
 *
 * @author 王攀博
 * @since 2024-04-23
 */
@Component
public class WaterFlowToolFactory implements ToolFactory {
    private final ToolFactory fitToolFactory;

    WaterFlowToolFactory(BrokerClient brokerClient, @Fit(alias = "json") ObjectSerializer serializer) {
        this.fitToolFactory = ToolFactory.fit(brokerClient, serializer);
    }

    @Nonnull
    @Override
    public String type() {
        return "WATERFLOW";
    }

    @Override
    public Tool create(Tool.Info toolInfo, Tool.Metadata metadata) {
        Tool tool = this.fitToolFactory.create(toolInfo, metadata);
        return new WaterFlowTool(tool, toolInfo, metadata);
    }
}
