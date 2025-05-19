/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop.parsers;

import static modelengine.fitframework.annotation.Order.LOW;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Order;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.value.ValueFetcher;
import modelengine.jade.carver.telemetry.aop.SpanAttrParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link SpanAttrParser} 的实现, 表示复杂属性值的解析器，支持结构体、列表和键值对。
 *
 * @author 马朝阳
 * @since 2024-07-29
 */
@Order(LOW)
@Component
public class ComplexSpanAttrParser implements SpanAttrParser {
    /**
     * 模式匹配格式：k:v1 或 k:v1,kk:v1.v2 或 k1:$.v1.v2 或 k1:[0].v1 或 k1:$[0].v1.v2
     */
    private static final Pattern FULL_PARSER_REG = Pattern.compile(
            "^\\s*\\w+:(\\$\\.|\\$?\\[\\d+\\]\\.|\\w+\\.)*\\w+\\s*(,\\s*\\w+:(\\$\\.|\\$?\\[\\d+\\]\\.|\\w+\\.)"
                    + "*\\w+\\s*)*$");
    private static final Logger LOGGER = Logger.get(ComplexSpanAttrParser.class);

    private final ValueFetcher fetcher;

    /**
     * 通过值获取工具来初始化 {@link ComplexSpanAttrParser} 的新实例。
     *
     * @param fetcher 表示值的获取工具的 {@link ValueFetcher}。
     */
    public ComplexSpanAttrParser(ValueFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * 解析器是否匹配。
     *
     * @param expression 表示注解表达式的 {@link String}。
     * @return 表示匹配结果的 {@code boolean}。
     */
    @Override
    public boolean match(String expression) {
        if (expression == null) {
            return false;
        }
        return FULL_PARSER_REG.matcher(expression).matches();
    }

    /**
     * 解析属性的键值对。
     *
     * @param expression 表示注解表达式的 {@link String}。
     * @param paramValue 表示被注解的参数值的 {@link Object}。
     * @return 表示属性的键值对的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    @Override
    public Map<String, String> parse(String expression, Object paramValue) {
        if (expression == null) {
            return Collections.emptyMap();
        }
        Map<String, String> expMap = new HashMap<>();
        for (String attr : expression.split(",")) {
            if (attr.isEmpty()) {
                LOGGER.warn("Split attribute fail, expression is {}.", expression);
                continue;
            }
            String[] kv = attr.split(":");
            if (kv.length != 2) {
                LOGGER.warn("Split key-value fail, attribute is {}.", attr);
                continue;
            }
            expMap.put(kv[0].trim(), kv[1].trim());
        }

        return expMap.entrySet().stream().peek(pair -> {
            Object parseRes = this.fetcher.fetch(paramValue, pair.getValue());
            pair.setValue(parseRes == null ? "" : parseRes.toString());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
