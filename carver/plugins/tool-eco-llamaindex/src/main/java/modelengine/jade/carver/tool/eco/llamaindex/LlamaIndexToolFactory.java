/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.eco.llamaindex;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolFactory;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示 {@link ToolFactory} 的 LlamaIndex 的实现。
 *
 * @author 刘信宏
 * @since 2024-06-24
 */
@Component
public class LlamaIndexToolFactory implements ToolFactory {
    private final ObjectSerializer serializer;
    private final BrokerClient brokerClient;

    public LlamaIndexToolFactory(@Fit(alias = "json") ObjectSerializer serializer, BrokerClient brokerClient) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.brokerClient = Validation.notNull(brokerClient, "The broker client cannot be null.");
    }

    @Override
    public String type() {
        return "LlamaIndex";
    }

    @Override
    public Tool create(Tool.Info itemInfo, Tool.Metadata metadata) {
        return new LlamaIndexTool(this.brokerClient, this.serializer, itemInfo, metadata);
    }
}
