/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.support.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import modelengine.fit.jade.aipp.tool.parallel.support.AippInstanceStatus;
import modelengine.fit.jane.common.entity.OperationContext;
import modelengine.fit.jober.aipp.constants.AippConst;
import modelengine.fit.jober.aipp.genericable.AippRunTimeService;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 查询实例状态的实现。
 *
 * @author 宋永坦
 * @since 2025-04-28
 */
@Component
public class AippInstanceStatusImpl implements AippInstanceStatus {
    private static final String DEFAULT_OPERATOR = "Jade";
    private static final OperationContext operationContext;

    private final Cache<String, Boolean> statusCache;
    private final AippRunTimeService aippRunTimeService;

    static {
        operationContext = new OperationContext();
        operationContext.setOperator(DEFAULT_OPERATOR);
    }

    public AippInstanceStatusImpl(@Fit AippRunTimeService aippRunTimeService) {
        this.aippRunTimeService = aippRunTimeService;
        this.statusCache = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).maximumSize(1000).build();
    }

    @Override
    public boolean isRunning(Map<String, Object> context) {
        String aippInstanceId = ObjectUtils.cast(ObjectUtils.nullIf(context, new HashMap<>())
                .getOrDefault(AippConst.CONTEXT_INSTANCE_ID, StringUtils.EMPTY));
        if (StringUtils.isBlank(aippInstanceId)) {
            // 如果上下文中不传入该值，则认为不需要根据实例状态控制退出。
            return true;
        }
        return Boolean.TRUE.equals(statusCache.get(aippInstanceId,
                __ -> this.aippRunTimeService.isInstanceRunning(aippInstanceId, operationContext)));
    }
}
