/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.eco.huggingface;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolFactory;
import modelengine.fel.service.pipeline.HuggingFacePipelineService;

/**
 * 表示 {@link ToolFactory} 的 HuggingFace 的实现。
 *
 * @author 季聿阶
 * @since 2024-06-04
 */
@Component
public class HuggingFaceToolFactory implements ToolFactory {
    private final ObjectSerializer serializer;
    private final HuggingFacePipelineService pipelineService;

    HuggingFaceToolFactory(@Fit(alias = "json") ObjectSerializer serializer,
            HuggingFacePipelineService pipelineService) {
        this.serializer = notNull(serializer, "The serializer cannot be null.");
        this.pipelineService = notNull(pipelineService, "The hugging face pipeline service cannot be null.");
    }

    @Nonnull
    @Override
    public String type() {
        return "HuggingFace";
    }

    @Override
    public Tool create(Tool.Info itemInfo, Tool.Metadata metadata) {
        return new HuggingFaceTool(this.serializer, this.pipelineService, itemInfo, metadata);
    }
}
