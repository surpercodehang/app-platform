/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.tool.repository.pgsql.model.entity;

import static modelengine.jade.carver.util.SerializeUtils.json2obj;

import modelengine.fel.tool.Tool;

import lombok.Data;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 存入数据库的定义的实体类。
 *
 * @author 王攀博
 * @since 2024-10-25
 */
@Data
public class DefinitionDo {
    private String schema;
    private String name;
    private String definitionGroupName;
    private Long definitionId;

    /**
     * 将领域对象转换为数据对象实体类。
     *
     * @param metadata 表示领域类的 {@link Tool.Metadata}。
     * @param serializer 表示序列化对象的 {@link ObjectSerializer}。
     * @return 工具定义实体类的 {@link ToolDo}。
     */
    public static DefinitionDo info2Do(Tool.Metadata metadata, ObjectSerializer serializer) {
        DefinitionDo definitionDo = new DefinitionDo();
        definitionDo.setDefinitionGroupName(metadata.definitionGroupName());
        definitionDo.setName(metadata.definitionName());
        definitionDo.setSchema(serializer.serialize(metadata.schema()));
        return definitionDo;
    }

    /**
     * 表示从数据对象转换为领域对象。
     *
     * @param definitionDo 表示从数据库获取的数据对象的 {@link DefinitionDo}。
     * @param serializer 表示序列化对象的 {@link ObjectSerializer}。
     * @return 表示工具定义领域对象的 {@link Tool.Metadata}。
     */
    public static Tool.Metadata do2Info(DefinitionDo definitionDo, ObjectSerializer serializer) {
        return Tool.Metadata.fromSchema(definitionDo.getDefinitionGroupName(),
                json2obj(definitionDo.getSchema(), serializer));
    }
}
