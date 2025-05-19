/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modelengine.jade.carver.entity.CommonDo;

/**
 * 基本组的实体类。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDo extends CommonDo {
    private String name;
    private String summary;
    private String description;
    private String extensions;
}
