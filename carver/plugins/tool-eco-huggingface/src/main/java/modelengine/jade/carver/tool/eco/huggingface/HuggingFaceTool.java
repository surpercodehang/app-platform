/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.eco.huggingface;

import static modelengine.fitframework.inspection.Validation.isInstanceOf;
import static modelengine.fitframework.inspection.Validation.isTrue;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.service.pipeline.HuggingFacePipelineService;
import modelengine.fel.tool.Tool;
import modelengine.fel.tool.eco.AbstractTaskTool;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表示 {@link Tool} 的 <a href="https://huggingface.co/">HuggingFace</a> 的实现。
 *
 * @author 季聿阶
 * @since 2024-06-04
 */
public class HuggingFaceTool extends AbstractTaskTool {
    private final HuggingFacePipelineService pipelineService;

    /**
     * 通过 Json 序列化器、工具的基本信息和工具元数据来初始化 {@link HuggingFaceTool} 的新实例。
     *
     * @param serializer 表示 Json 序列化器的 {@link ObjectSerializer}。
     * @param pipelineService 表示 HuggingFace 提供的流水线服务的 {@link HuggingFacePipelineService}。
     * @param itemInfo 表示工具的基本信息的 {@link Info}。
     * @param metadata 表示工具的元数据的 {@link Metadata}。
     */
    protected HuggingFaceTool(ObjectSerializer serializer, HuggingFacePipelineService pipelineService,
            Tool.Info itemInfo, Metadata metadata) {
        super(serializer, itemInfo, metadata);
        this.pipelineService = notNull(pipelineService, "The hugging face pipeline service cannot be null.");
    }

    @Override
    public Object executeWithTask(String taskId, Object... args) {
        notNull(args, "The call args cannot be null.");
        isTrue(args.length >= 1, "The call args must have 1 arg.");
        String model = isInstanceOf(args[0], String.class, "The first arg must be String.class.");
        List<String> actualNames = this.metadata().parameterOrder().stream().skip(2).collect(Collectors.toList());
        isTrue(actualNames.size() == args.length - 1, "The arg names do not match the actual args.");
        Map<String, Object> actualMapArg = new HashMap<>();
        for (int i = 0; i < actualNames.size(); i++) {
            actualMapArg.put(actualNames.get(i), args[i + 1]);
        }
        return this.pipelineService.call(taskId, model, actualMapArg);
    }
}
