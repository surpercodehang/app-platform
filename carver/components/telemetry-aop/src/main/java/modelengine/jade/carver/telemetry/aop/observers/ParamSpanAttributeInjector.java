/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop.observers;

import io.opentelemetry.api.trace.Span;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.jade.carver.telemetry.aop.SpanAttrObserver;
import modelengine.jade.carver.telemetry.aop.SpanAttrParser;
import modelengine.jade.carver.telemetry.aop.SpanAttrParserRepository;
import modelengine.jade.carver.telemetry.aop.SpanEndObserver;
import modelengine.jade.service.annotations.SpanAttr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 向 span 中注入 {@link SpanAttr} 注解的属性键值对。
 *
 * @author 方誉州
 * @since 2024-08-06
 */
@Component(name = "ParamSpanAttributeInjector")
public class ParamSpanAttributeInjector implements SpanEndObserver, SpanAttrObserver {
    private final SpanAttrParserRepository repository;

    public ParamSpanAttributeInjector(SpanAttrParserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onSpanEnd(Span span, Method method, Object[] args, Object result) {
        onAppendSpanAttr(span, method, args, result);
    }

    private void setAttribute(Span span, String[] expressions, Object paramValue) {
        List<SpanAttrParser> parsers = this.repository.get();
        for (String expression : expressions) {
            Map<String, String> attributeMap = parsers.stream()
                    .filter(parser -> parser.match(expression))
                    .findFirst()
                    .map(parser -> parser.parse(expression, paramValue))
                    .orElseGet(Collections::emptyMap);
            attributeMap.forEach(span::setAttribute);
        }
    }

    @Override
    public void onAppendSpanAttr(Span span, Method method, Object[] args, Object result) {
        if (span == null) {
            return;
        }
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int index = 0; index < parameterAnnotations.length; index++) {
            int currentIndex = index;
            Arrays.stream(parameterAnnotations[index])
                    .filter(annotation -> annotation.annotationType() == SpanAttr.class)
                    .map(ObjectUtils::<SpanAttr>cast)
                    .forEach(annotation -> this.setAttribute(span, annotation.value(), args[currentIndex]));
        }
    }
}