/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.repository.pgsql.repository.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fel.tool.model.entity.ToolIdentifier;
import modelengine.jade.store.entity.query.ToolQuery;
import modelengine.jade.store.repository.pgsql.mapper.StoreToolMapper;
import modelengine.jade.store.repository.pgsql.repository.StoreToolRepository;

import java.util.List;

/**
 * 表示默认包含额外信息的工具的仓库。
 *
 * @author 李金绪
 * @since 2024-09-14
 */
@Component
public class DefaultStoreToolRepository implements StoreToolRepository {
    private static final Logger log = Logger.get(DefaultStoreToolRepository.class);

    private final ObjectSerializer serializer;
    private final StoreToolMapper storeToolMapper;

    /**
     * 通过 mapper 接口来初始化 {@link DefaultStoreToolRepository} 的实例。
     *
     * @param serializer 表示序列化对象的 {@link ObjectSerializer}。
     * @param storeToolMapper 标识工具的 mapper 接口的 {@link StoreToolMapper}。
     */
    public DefaultStoreToolRepository(@Fit(alias = "json") ObjectSerializer serializer,
            StoreToolMapper storeToolMapper) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.storeToolMapper = notNull(storeToolMapper, "The store tool mapper cannot be null.");
    }

    @Override
    public List<ToolIdentifier> getTools(ToolQuery toolQuery) {
        return this.storeToolMapper.getTools(toolQuery);
    }

    @Override
    public List<ToolIdentifier> searchTools(ToolQuery toolQuery) {
        return this.storeToolMapper.searchTools(toolQuery);
    }

    @Override
    public int getToolsCount(ToolQuery toolQuery) {
        return storeToolMapper.getToolsCount(toolQuery);
    }

    @Override
    public int searchToolsCount(ToolQuery toolQuery) {
        return storeToolMapper.searchToolsCount(toolQuery);
    }
}
