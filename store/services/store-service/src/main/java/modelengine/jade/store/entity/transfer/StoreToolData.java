/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.store.entity.transfer;

import modelengine.fel.tool.model.transfer.ToolData;

import java.util.Set;

/**
 * 表示包含额外信息的工具数据内容。
 *
 * @author 李金绪
 * @since 2024-09-13
 */
public class StoreToolData extends ToolData {
    /**
     * 表示工具的标签集合。
     */
    private Set<String> tags;

    /**
     * 表示工具的来源。
     */
    private String source;

    /**
     * 表示工具的图标。
     */
    private String icon;

    /**
     * 表示工具的创建者。
     */
    private String creator;

    /**
     * 表示工具的修改者。
     */
    private String modifier;

    /**
     * 获取工具的标签集合。
     *
     * @return 表示工具的标签集合的 {@link Set}{@code <}{@link String}{@code >}。
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * 设置工具的标签集合。
     *
     * @param tags 表示工具的标签集合的 {@link Set}{@code <}{@link String}{@code >}。
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * 获取工具的来源。
     *
     * @return 表示工具的来源的 {@link String}。
     */
    public String getSource() {
        return this.source;
    }

    /**
     * 设置工具的来源。
     *
     * @param source 表示工具的来源的 {@link String}。
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取工具的图标。
     *
     * @return 表示工具的图标的 {@link String}。
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * 设置工具的图标。
     *
     * @param icon 表示工具的图标的 {@link String}。
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取工具的创建者。
     *
     * @return 表示工具的创建者的 {@link String}。
     */
    public String getCreator() {
        return this.creator;
    }

    /**
     * 设置工具的创建者。
     *
     * @param creator 表示工具的创建者的 {@link String}。
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取工具的修改者。
     *
     * @return 表示工具的修改者的 {@link String}。
     */
    public String getModifier() {
        return this.modifier;
    }

    /**
     * 设置工具的修改者。
     *
     * @param modifier 表示工具的修改者的 {@link String}。
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     * 从给定的工具数据和标签创建一个新的 {@link StoreToolData} 实例。
     *
     * @param toolData 表示工具数据的 {@link ToolData}。
     * @param tags 表示标签的 {@link Set}{@code <}{@link String}{@code >}。
     * @return 表示包含额外信息的工具数据 {@link StoreToolData}。
     */
    public static StoreToolData from(ToolData toolData, Set<String> tags) {
        StoreToolData storeToolData = new StoreToolData();
        storeToolData.setName(toolData.getName());
        storeToolData.setUniqueName(toolData.getUniqueName());
        storeToolData.setDescription(toolData.getDescription());
        storeToolData.setSchema(toolData.getSchema());
        storeToolData.setRunnables(toolData.getRunnables());
        storeToolData.setExtensions(toolData.getExtensions());
        storeToolData.setVersion(toolData.getVersion());
        storeToolData.setLatest(toolData.getLatest());
        storeToolData.setTags(tags);
        return storeToolData;
    }
}
