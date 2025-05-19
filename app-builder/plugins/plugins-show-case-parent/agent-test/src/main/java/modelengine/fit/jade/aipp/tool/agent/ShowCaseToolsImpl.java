/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package modelengine.fit.jade.aipp.tool.agent;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Property;
import modelengine.fel.tool.annotation.Attribute;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO
 *
 * @author 夏斐
 * @since 2025/3/13
 */
@Component
@Group(name = "AgentFunctionCallingShowCaseToolsImpl")
public class ShowCaseToolsImpl implements ShowCaseTools {
    private static final List<String> WEATHER_CONDITIONS = Arrays.asList(
            "晴空万里", "阳光明媚", "薄云漫天", "乌云密布", "细雨蒙蒙", "瓢泼大雨", "雷雨交加", "暴风骤雨", "和风徐徐",
            "狂风怒号", "迷雾重重", "雾霾笼罩", "白雪皑皑", "鹅毛大雪", "冰雹来袭"
    );

    private static final List<String> TRAFFIC_CONDITIONS = Arrays.asList(
            "道路畅通", "交通顺畅", "轻微拥堵", "交通缓行", "中度拥堵", "严重拥堵", "堵车严重", "交通事故", "道路施工",
            "封路管制"
    );

    private static final Map<String, String> COUNTRY_CAPITAL_MAP = new HashMap<>();

    static {
        COUNTRY_CAPITAL_MAP.put("中国", "北京");
        COUNTRY_CAPITAL_MAP.put("美国", "华盛顿");
        COUNTRY_CAPITAL_MAP.put("英国", "伦敦");
        COUNTRY_CAPITAL_MAP.put("法国", "巴黎");
        COUNTRY_CAPITAL_MAP.put("德国", "柏林");
        COUNTRY_CAPITAL_MAP.put("意大利", "罗马");
        COUNTRY_CAPITAL_MAP.put("西班牙", "马德里");
        COUNTRY_CAPITAL_MAP.put("俄罗斯", "莫斯科");
        COUNTRY_CAPITAL_MAP.put("日本", "东京");
        COUNTRY_CAPITAL_MAP.put("韩国", "首尔");
        COUNTRY_CAPITAL_MAP.put("印度", "新德里");
        COUNTRY_CAPITAL_MAP.put("加拿大", "渥太华");
        COUNTRY_CAPITAL_MAP.put("澳大利亚", "堪培拉");
        COUNTRY_CAPITAL_MAP.put("巴西", "巴西利亚");
        COUNTRY_CAPITAL_MAP.put("阿根廷", "布宜诺斯艾利斯");
        COUNTRY_CAPITAL_MAP.put("墨西哥", "墨西哥城");
        COUNTRY_CAPITAL_MAP.put("埃及", "开罗");
        COUNTRY_CAPITAL_MAP.put("南非", "比勒陀利亚");
        COUNTRY_CAPITAL_MAP.put("土耳其", "安卡拉");
        COUNTRY_CAPITAL_MAP.put("沙特阿拉伯", "利雅得");
    }

    private final Random random = new Random();

    @Override
    @Fitable("default")
    @ToolMethod(name = "weatherDefault", description = "用于查询指定地区的天气情况", extensions = {
            @Attribute(key = "tags", value = "FIT")
    })
    @Property(description = "查询指定地区的天气情况的结果")
    public String weather(String city) {
        return WEATHER_CONDITIONS.get(random.nextInt(WEATHER_CONDITIONS.size()));
    }

    @Override
    @Fitable("default")
    @ToolMethod(name = "trafficDefault", description = "用于查询指定地区的交通情况", extensions = {
            @Attribute(key = "tags", value = "FIT")
    })
    @Property(description = "查询指定地区的交通情况的结果")
    public String traffic(String city) {
        return TRAFFIC_CONDITIONS.get(random.nextInt(TRAFFIC_CONDITIONS.size()));
    }

    @Override
    @Fitable("default")
    @ToolMethod(name = "capitalDefault", description = "用于查询指定国家的首都", extensions = {
            @Attribute(key = "tags", value = "FIT")
    })
    @Property(description = "查询指定国家的首都的结果")
    public String capital(String country) {
        return COUNTRY_CAPITAL_MAP.getOrDefault(country, "未知国家或地区");
    }
}
