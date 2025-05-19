/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop.stub;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.jade.carver.telemetry.aop.CarverSpanAspect;
import modelengine.jade.service.annotations.CarverSpan;
import modelengine.jade.service.annotations.SpanAttr;

/**
 * 为 {@link CarverSpanAspect} 测试创建的类，测试复杂对象调用流程。
 *
 * @author 马朝阳
 * @since 2024-07-31
 */
@Component
public class CarverSpanParserDemo {
    private static final Logger log = Logger.get(CarverSpanParserDemo.class);

    private static final String BASE_ATTRIBUTE_KEY = "player:k1";
    private static final String KV_ATTRIBUTE_KEY = "player:k1.k2,player2:k11.k22";
    private static final String OBJECT_ATTRIBUTE_KEY = "player:k1,player2:$.k1.k2,player3:k1.k2";
    private static final String LIST_ATTRIBUTE_KEY = "player:[0].k1,player2:$[0].k1.k2,player3:[0].k2";

    /**
     * 基本数据类型解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.primitive.parser")
    public void handlePrimitiveParser(@SpanAttr(BASE_ATTRIBUTE_KEY) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 键值对解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.kv.parser")
    public void handleKVParser(@SpanAttr(KV_ATTRIBUTE_KEY) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 对象解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.object.parser")
    public void handleObjectParser(@SpanAttr(OBJECT_ATTRIBUTE_KEY) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 列表解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.list.parser")
    public void handleListParser(@SpanAttr(LIST_ATTRIBUTE_KEY) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 属性数组：键值对解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.kv.parser")
    public void handleMultiKVParser(@SpanAttr({"player:k1.k2", "player2:k11.k22"}) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 属性数组：对象解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.object.parser")
    public void handleMultiObjectParser(
            @SpanAttr({"player:k1", "player:k1,player2:$.k1.k2", "player3:k1.k2", "player4"}) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 属性数组：列表解析操作。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.list.parser")
    public void handleMultiListParser(
            @SpanAttr({"player:[0].k1", "player2:$[0].k1.k2", "player3:[0].k2"}) Object player) {
        log.debug("input param: {}", player);
    }

    /**
     * 属性数组：数组为空。
     *
     * @param player 表示复杂操作参数的 {@link Object}。
     */
    @CarverSpan(value = "operation.list.parser")
    public void handleEmptyArrayParser(@SpanAttr Object player) {
        log.debug("input param: {}", player);
    }
}
