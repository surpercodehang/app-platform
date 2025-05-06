/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.support.impl;

import modelengine.fit.jade.aipp.tool.parallel.domain.BatchRequest;
import modelengine.fit.jade.aipp.tool.parallel.support.TaskExecutor;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.thread.DefaultThreadFactory;
import modelengine.fitframework.util.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务执行器的实现。
 *
 * @author 宋永坦
 * @since 2025-04-24
 */
@Component
public class DefaultTaskExecutor implements TaskExecutor {
    private static final Logger LOG = Logger.get(BatchRequest.class);
    private static final int MIN_THREAD_POOL_SIZE = 1;
    private static final int MAX_THREAD_POOL_SIZE = 128;
    private static final int MIN_THREAD_CORE_SIZE = 0;

    private final ExecutorService executorService;

    public DefaultTaskExecutor(@Value("${parallel-tool.thread-pool-size:64}") int threadPoolSize,
            @Value("${parallel-tool.thread-core-size:8}") int threadCoreSize) {
        Validation.between(threadPoolSize,
                MIN_THREAD_POOL_SIZE,
                MAX_THREAD_POOL_SIZE,
                StringUtils.format("The parallel tool thread pool size should between {0} and {1}.",
                        MIN_THREAD_POOL_SIZE,
                        MAX_THREAD_POOL_SIZE));
        Validation.between(threadCoreSize,
                MIN_THREAD_CORE_SIZE,
                threadPoolSize,
                StringUtils.format("The parallel tool thread core size should between {0} and {1}.",
                        MIN_THREAD_CORE_SIZE,
                        threadPoolSize));
        this.executorService = new ThreadPoolExecutor(threadCoreSize,
                threadPoolSize,
                5L,
                TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new DefaultThreadFactory("parallel-tool", false, (thread, throwable) -> {
                    LOG.error("[parallel-tool] Exception. [message={}]", throwable.getMessage());
                    LOG.error("[parallel-tool] Details:", throwable);
                }),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void post(Runnable runnable) {
        this.executorService.execute(runnable);
    }
}
