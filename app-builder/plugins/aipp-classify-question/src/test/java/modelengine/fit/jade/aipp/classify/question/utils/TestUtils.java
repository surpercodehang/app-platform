/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.jade.aipp.classify.question.utils;

import static modelengine.fit.jade.aipp.classify.question.util.Constant.QUESTION_KEY;

import modelengine.fit.jade.aipp.classify.question.QuestionType;
import modelengine.fit.jade.aipp.classify.question.command.ClassifyQuestionCommand;
import modelengine.fit.jade.aipp.memory.AippMemoryConfig;
import modelengine.fitframework.util.MapBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试使用工具类。
 *
 * @author 张越
 * @since 2024-11-18
 */
public class TestUtils {
    /**
     * 获取问题分类命令。
     *
     * @return 表示问题分类命令的 {@link ClassifyQuestionCommand}。
     */
    public static ClassifyQuestionCommand getCommand() {
        ClassifyQuestionCommand command = new ClassifyQuestionCommand();
        command.setArgs(MapBuilder.<String, String>get().put(QUESTION_KEY, "sky").build());
        command.setTemplate("");
        command.setModel("model");
        command.setTemperature(0.1);

        // 设置memory.
        AippMemoryConfig config = new AippMemoryConfig();
        config.setWindowAlg("buffer_window");
        config.setSerializeAlg("full");
        config.setProperty(3);
        command.setMemoryConfig(config);

        // 设置问题类型.
        List<QuestionType> questionTypeList = new ArrayList<>();
        QuestionType questionType = new QuestionType();
        questionType.setId("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        questionType.setQuestionTypeDesc("a");
        questionTypeList.add(questionType);
        QuestionType questionType1 = new QuestionType();
        questionType1.setId("3fa4e1b2-7c6d-4a9f-8c3d-1b2e3f4a5b6c");
        questionType1.setQuestionTypeDesc("b");
        questionTypeList.add(questionType1);
        command.setQuestionTypes(questionTypeList);
        return command;
    }
}