/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.execution.controller;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.service.ToolExecuteService;

import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fitframework.annotation.Component;

/**
 * 表示对外提供工具执行的 HTTP 调用接口。
 *
 * @author 王攀博
 * @since 2024-04-29
 */
@Component
public class ToolExecutionController {
    private final ToolExecuteService toolExecuteService;

    ToolExecutionController(ToolExecuteService toolExecuteService) {
        this.toolExecuteService = notNull(toolExecuteService, "The execution service cannot be null.");
    }

    /**
     * 执行工具的请求。
     *
     * @param platform 表示发送请求的平台的 {@link String}。
     * @param uniqueName 表示工具唯一名称的 {@link String}。
     * @param jsonArgs 表示工具执行参数作为消息体传入的 {@link String}。
     * @return 表示工具执行结果的 {@link String}。
     */
    @PostMapping(path = "/store/platform/{platform}/exec")
    public String toolExecute(@PathVariable("platform") String platform, @RequestParam("uniqueName") String uniqueName,
            @RequestBody String jsonArgs) {
        return this.toolExecuteService.execute(uniqueName, jsonArgs);
    }
}