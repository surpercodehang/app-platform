/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.model.entity;

import static modelengine.jade.carver.util.SerializeUtils.json2obj;

import modelengine.fel.tool.model.transfer.ToolGroupData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 存入数据库的工具组的实体类。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolGroupDo extends GroupDo {
    private String definitionGroupName;

    /**
     * 将数据对象实体类象转换为传输对象。
     *
     * @param groupDo 表示工具组实体类的 {@link ToolGroupDo}。
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @return 表示工具组传输对象的 {@link ToolGroupData}。
     */
    public static ToolGroupData do2Data(ToolGroupDo groupDo, ObjectSerializer serializer) {
        ToolGroupData groupData = new ToolGroupData();
        groupData.setName(groupDo.getName());
        groupData.setSummary(groupDo.getSummary());
        groupData.setDescription(groupDo.getDescription());
        groupData.setExtensions(json2obj(groupDo.getExtensions(), serializer));
        groupData.setDefGroupName(groupDo.getDefinitionGroupName());
        return groupData;
    }

    /**
     * 将传输对象转换为数据对象实体类。
     *
     * @param groupData 表示工具组传输对象的 {@link ToolGroupData}。
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @return 表示工具组实体类的 {@link ToolGroupDo}。
     */
    public static ToolGroupDo data2Do(ToolGroupData groupData, ObjectSerializer serializer) {
        ToolGroupDo groupDo = new ToolGroupDo();
        groupDo.setName(groupData.getName());
        groupDo.setSummary(groupData.getSummary());
        groupDo.setDescription(groupData.getDescription());
        groupDo.setExtensions(serializer.serialize(groupData.getExtensions()));
        groupDo.setDefinitionGroupName(groupData.getDefGroupName());
        return groupDo;
    }
}
