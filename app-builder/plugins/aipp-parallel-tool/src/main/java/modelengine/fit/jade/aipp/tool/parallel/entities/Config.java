/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.tool.parallel.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 并行工具调用的配置。
 *
 * @author 宋永坦
 * @since 2025-04-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private Integer concurrency;
}
