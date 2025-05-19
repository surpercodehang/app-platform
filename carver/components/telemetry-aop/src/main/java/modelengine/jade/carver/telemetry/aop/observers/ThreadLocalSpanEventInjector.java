/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop.observers;

import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYSTEM_ATTRIBUTE_EVENT_NAME;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import modelengine.fitframework.annotation.Component;
import modelengine.jade.authentication.context.UserContext;
import modelengine.jade.authentication.context.UserContextHolder;
import modelengine.jade.carver.operation.enums.OperationLogConstant;
import modelengine.jade.carver.telemetry.aop.SpanEndObserver;

import java.lang.reflect.Method;

/**
 * 收集 threadLocal 数据并向 span 中添加事件属性。
 *
 * @author 方誉州
 * @since 2024-08-06
 */
@Component
public class ThreadLocalSpanEventInjector implements SpanEndObserver {
    /**
     * 收集 threadLocal 上的数据并添加为 span 的事件属性。
     *
     * @param span 表示待添加事件的{@link Span}。
     * @param method 表示被拦截的方法的{@link Method}。
     * @param args 表示被拦截方法的参数列表的{@link Object}{@code []}。
     * @param result 表示被拦截方法的返回结果的{@link Object}。
     */
    @Override
    public void onSpanEnd(Span span, Method method, Object[] args, Object result) {
        if (span == null) {
            return;
        }
        UserContext userContext = UserContextHolder.get();
        // 从threadLocal中获取
        span.addEvent(SYSTEM_ATTRIBUTE_EVENT_NAME,
                Attributes.of(AttributeKey.stringKey(OperationLogConstant.SYS_OP_LANGUAGE_KEY),
                        userContext.getLanguage(),
                        AttributeKey.stringKey(OperationLogConstant.SYS_OP_OPERATOR_KEY),
                        userContext.getName(),
                        AttributeKey.stringKey(OperationLogConstant.SYS_OP_IPADDR_KEY),
                        userContext.getIp()));
    }
}
