/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.model.entity;

import static modelengine.jade.carver.util.SerializeUtils.json2obj;

import modelengine.fel.tool.model.transfer.DefinitionGroupData;

import lombok.AllArgsConstructor;
import lombok.Data;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 存入数据库的定义组的实体类。
 *
 * @author 李金绪
 * @since 2024-12-10
 */
@Data
@AllArgsConstructor
public class DefinitionGroupDo extends GroupDo {
    /**
     * 将数据对象实体类象转换为传输对象。
     *
     * @param groupDo 表示定义组实体类的 {@link DefinitionGroupDo}。
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @return 表示定义组传输对象的 {@link DefinitionGroupData}。
     */
    public static DefinitionGroupData do2Data(DefinitionGroupDo groupDo, ObjectSerializer serializer) {
        DefinitionGroupData groupData = new DefinitionGroupData();
        groupData.setName(groupDo.getName());
        groupData.setSummary(groupDo.getSummary());
        groupData.setDescription(groupDo.getDescription());
        groupData.setExtensions(json2obj(groupDo.getExtensions(), serializer));
        return groupData;
    }

    /**
     * 将传输对象转换为数据对象实体类。
     *
     * @param groupData 表示定义组传输对象的 {@link DefinitionGroupData}。
     * @param serializer 表示序列化器的 {@link ObjectSerializer}。
     * @return 表示定义组实体类的 {@link DefinitionGroupDo}。
     */
    public static DefinitionGroupDo data2Do(DefinitionGroupData groupData, ObjectSerializer serializer) {
        DefinitionGroupDo groupDo = new DefinitionGroupDo();
        groupDo.setName(groupData.getName());
        groupDo.setSummary(groupData.getSummary());
        groupDo.setDescription(groupData.getDescription());
        groupDo.setExtensions(serializer.serialize(groupData.getExtensions()));
        return groupDo;
    }
}
