/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package modelengine.fit.jade.aipp.tool.agent;

import modelengine.fitframework.annotation.Genericable;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;

/**
 * TODO
 *
 * @author 夏斐
 * @since 2025/3/13
 */
@Group(name = "AgentFunctionCallingShowCaseTools")
public interface ShowCaseTools {
    @ToolMethod(name = "weather", description = "用于查询指定地区的天气情况")
    @Genericable("modelengine.jober.aipp.tool.agent.test.weather")
    String weather(String city);

    @ToolMethod(name = "traffic", description = "用于查询指定地区的交通情况")
    @Genericable("modelengine.jober.aipp.tool.agent.test.traffic")
    String traffic(String city);

    @ToolMethod(name = "capital", description = "用于查询指定国家的首都")
    @Genericable("modelengine.jober.aipp.tool.agent.test.capital")
    String capital(String country);
}
