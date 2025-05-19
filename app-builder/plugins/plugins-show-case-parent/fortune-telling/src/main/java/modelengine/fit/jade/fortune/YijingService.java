/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.jade.fortune;

import modelengine.fitframework.annotation.Genericable;
import modelengine.fitframework.annotation.Property;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;

/**
 * 易经服务接口。
 *
 * @author 杭潇
 * @since 2025-03-19
 */
@Group(name = "Yijing")
public interface YijingService {
    @ToolMethod(name = "算命查询工具", description = "用于易经信息查询")
    @Genericable("modelengine.jober.aipp.fortune")
    String getForTune(@Property(description = "易经计算的key值", required = true) String args);
}
