/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.repository.support;

import modelengine.fel.tool.ToolFactory;
import modelengine.fel.tool.ToolFactoryRepository;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表示工厂的存储库。
 *
 * @author 李金绪
 * @since 2024/5/16
 */
@Component
public class DefaultToolFactoryRepository implements ToolFactoryRepository {
    private final Map<String, ToolFactory> tagFactoryMapping = new ConcurrentHashMap<>();

    @Override
    public Optional<ToolFactory> match(Set<String> runnable) {
        if (CollectionUtils.isEmpty(runnable)) {
            return Optional.empty();
        }
        return runnable.stream()
                .map(String::toUpperCase)
                .filter(this.tagFactoryMapping::containsKey)
                .map(this.tagFactoryMapping::get)
                .findFirst();
    }

    @Override
    public void register(ToolFactory toolFactory) {
        if (toolFactory == null) {
            return;
        }
        this.tagFactoryMapping.put(StringUtils.toUpperCase(toolFactory.type()), toolFactory);
    }

    @Override
    public void unregister(ToolFactory toolFactory) {
        if (toolFactory == null) {
            return;
        }
        this.tagFactoryMapping.remove(StringUtils.toUpperCase(toolFactory.type()));
    }
}
