/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.eco.langchain;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.eco.AbstractKvTool;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示 {@link Tool} 的
 * <a href="https://python.langchain.com/v0.2/docs/integrations/tools/">LangChain</a> 的实现。
 *
 * @author 刘信宏
 * @since 2024-06-19
 */
public class LangChainTool extends AbstractKvTool {
    /**
     * 通过 Json 序列化器、工具的基本信息和工具元数据来初始化 {@link LangChainTool} 的新实例。
     *
     * @param brokerClient 表示服务调用的代理客户端的 {@link BrokerClient}。
     * @param serializer 表示 Json 序列化器的 {@link ObjectSerializer}。
     * @param itemInfo 表示工具的基本信息的 {@link Tool.Info}。
     * @param metadata 表示工具的元数据的 {@link Metadata}。
     */
    public LangChainTool(BrokerClient brokerClient, ObjectSerializer serializer, Tool.Info itemInfo,
            Metadata metadata) {
        super(brokerClient, serializer, itemInfo, metadata);
    }

    @Override
    protected String type() {
        return "LangChain";
    }
}
