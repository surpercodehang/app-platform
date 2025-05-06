/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.support;

import java.util.Map;

/**
 * 查询实例状态的接口。
 *
 * @author 宋永坦
 * @since 2025-04-28
 */
public interface AippInstanceStatus {
    /**
     * 根据提供的上下文信息查询实例是否正在运行。
     *
     * @param context 表示上下文信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。。
     * @return 表示是否正在运行状态的 {@code boolean}。
     */
    boolean isRunning(Map<String, Object> context);
}
