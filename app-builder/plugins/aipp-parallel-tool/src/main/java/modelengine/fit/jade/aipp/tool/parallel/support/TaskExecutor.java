/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.support;

/**
 * 任务执行器的接口。
 *
 * @author 宋永坦
 * @since 2025-04-23
 */
public interface TaskExecutor {
    /**
     * 投递任务。
     *
     * @param runnable 表示需要执行任务的 {@link Runnable}。
     */
    void post(Runnable runnable);
}
