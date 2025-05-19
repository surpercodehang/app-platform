/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.jade.fortune;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Property;
import modelengine.fel.tool.annotation.Attribute;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;

import java.util.Map;

/**
 * 易经服务实现。
 *
 * @author 杭潇
 * @since 2025-03-19
 */
@Group(name = "YijingImpl")
@Component
public class YijingServiceImpl implements YijingService {
    private final Map<String, String> fortune;

    public YijingServiceImpl() {
        ExcelToMapSingleton loader = ExcelToMapSingleton.getInstance();
        loader.loadExcelData();
        this.fortune = loader.getDataMap();
    }

    @Override
    @Fitable("default")
    @ToolMethod(name = "算命查询工具", description = "用于查询易经信息", extensions = {
            @Attribute(key = "tags", value = "FIT")
    })
    @Property(description = "易经查询结果")
    public String getForTune(String args) {
        return this.fortune.get(args);
    }
}
