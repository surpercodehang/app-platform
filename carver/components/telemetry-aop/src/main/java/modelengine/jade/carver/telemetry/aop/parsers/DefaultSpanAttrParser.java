/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop.parsers;

import static modelengine.fitframework.annotation.Order.LOWEST;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Order;
import modelengine.fitframework.util.StringUtils;
import modelengine.jade.carver.telemetry.aop.SpanAttrParser;

import java.util.Collections;
import java.util.Map;

/**
 * {@link SpanAttrParser} 默认实现。
 *
 * @author 刘信宏
 * @since 2024-07-25
 */
@Order(LOWEST)
@Component
public class DefaultSpanAttrParser implements SpanAttrParser {
    @Override
    public boolean match(String expression) {
        return expression != null;
    }

    @Override
    public Map<String, String> parse(String expression, Object paramValue) {
        if (expression == null) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(expression, paramValue == null ? StringUtils.EMPTY : paramValue.toString());
    }
}
