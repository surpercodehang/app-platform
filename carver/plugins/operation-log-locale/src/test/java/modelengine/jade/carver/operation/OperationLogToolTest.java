/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fitframework.globalization.StringResource;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.util.MapBuilder;
import modelengine.jade.carver.operation.enums.OperationLogConstant;
import modelengine.jade.carver.operation.support.CompositParam;
import modelengine.jade.carver.operation.support.OperationLogFields;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

/**
 * 操作日志国际化工具测试类。
 *
 * @author 方誉州
 * @since 2024-08-01
 */
public class OperationLogToolTest {
    private static CompositParam params;
    private static Map<String, String> resources;

    Plugin pluginMock = mock(Plugin.class, RETURNS_DEEP_STUBS);
    StringResource srMock = mock(StringResource.class);

    @BeforeAll
    static void setUpResource() {
        Map<String, String> userAttribute =
                MapBuilder.<String, String>get().put("key", "hello").put("value", "world").build();
        Map<String, String> systemAttribute = MapBuilder.<String, String>get()
                .put(OperationLogConstant.SYS_OP_RESULT_KEY, OperationLogConstant.SYS_OP_SUCCEED)
                .put(OperationLogConstant.SYS_OP_IPADDR_KEY, "127.0.0.1")
                .put(OperationLogConstant.SYS_OP_LANGUAGE_KEY, "en")
                .put(OperationLogConstant.SYS_OP_OPERATOR_KEY, "Admin")
                .build();
        params = new CompositParam(userAttribute, systemAttribute);
        resources = MapBuilder.<String, String>get()
                .put("base", "hello")
                .put("base.level", "MINOR")
                .put("base.module", "OperationLogToolTest")
                .put("base.resource", "testGetLocaleMessage")
                .put("base.uri", "/test")
                .put("base.result", "success")
                .put("base.succeed.detail", "{{key}} : {{value}}")
                .build();
    }

    @BeforeEach
    void setUp() {
        when(this.pluginMock.sr()).thenReturn(this.srMock);
        when(this.srMock.getMessage(any(), anyString())).thenAnswer((invocation) -> {
            String key = invocation.getArgument(1);
            return resources.get(key);
        });
    }

    @Test
    @DisplayName("测试获取字符串资源")
    void testGetMessage() {
        Locale locale = new Locale("en");
        OperationLogLocaleServiceImpl.MessageGetter messgaeGetter =
                new OperationLogLocaleServiceImpl.MessageGetter(pluginMock, "base", locale);
        assertThat(messgaeGetter).extracting(obj -> obj.get(""),
                        obj -> obj.get("result"),
                        obj -> obj.get("succeed.detail", params.getUserAttribute()))
                .containsExactly("hello", "success", "hello : world");
    }

    @Test
    @DisplayName("测试获取成功的国际化操作日志")
    void testGetLocaleMessageSuccess() {
        OperationLogLocaleService localeService = new OperationLogLocaleServiceImpl(pluginMock);
        OperationLogFields fields = localeService.getLocaleMessage("base", params);
        assertThat(fields).extracting(OperationLogFields::getName,
                        OperationLogFields::getLevel,
                        OperationLogFields::getFunctionModule,
                        OperationLogFields::getResourceName,
                        OperationLogFields::getRequestUri,
                        OperationLogFields::getOperationResult,
                        OperationLogFields::getDetails)
                .containsExactly("hello",
                        "MINOR",
                        "OperationLogToolTest",
                        "testGetLocaleMessage",
                        "/test",
                        "SUCCESS",
                        "hello : world");
    }
}
