/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.operation;

import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_IPADDR_KEY;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_LANGUAGE_KEY;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_OPERATOR_KEY;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_RESULT_KEY;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_SUCCEED;

import modelengine.fel.core.template.support.DefaultStringTemplate;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.jade.carver.operation.support.CompositParam;
import modelengine.jade.carver.operation.support.OperationLogFields;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 操作日志国际化工具。
 *
 * @author 方誉州
 * @since 2024-07-30
 */
@Component
public class OperationLogLocaleServiceImpl implements OperationLogLocaleService {
    private static final String FUNCTION_MODULE_KEY = "module";
    private static final String LEVEL_KEY = "level";
    private static final String SUCCESS_DETAIL_KEY = "succeed.detail";
    private static final String FAIL_DETAIL_KEY = "failed.detail";
    private static final String TARGET_RESOURCE_KEY = "resource";
    private static final String URI_KEY = "uri";
    private static final List<Locale> LOCALES =
            Arrays.asList(new Locale("en"), new Locale("zh"), new Locale("en", "US"), new Locale("zh", "CN"));

    private final Plugin plugin;

    OperationLogLocaleServiceImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public OperationLogFields getLocaleMessage(String operation, CompositParam params) {
        Map<String, String> systemAttribute = ObjectUtils.getIfNull(params.getSystemAttribute(), Collections::emptyMap);
        String language = systemAttribute.get(SYS_OP_LANGUAGE_KEY);
        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(language);
        Locale locale = StringUtils.isNotEmpty(language) ? Locale.lookup(list, LOCALES) : Locale.getDefault();
        MessageGetter msgGetter = new MessageGetter(plugin, operation, locale);
        OperationLogFields.OperationLogFieldsBuilder builder = OperationLogFields.builder()
                .name(msgGetter.get())
                .level(msgGetter.get(LEVEL_KEY))
                .functionModule(msgGetter.get(FUNCTION_MODULE_KEY))
                .resourceName(msgGetter.get(TARGET_RESOURCE_KEY))
                .requestUri(msgGetter.get(URI_KEY))
                .operationResult(systemAttribute.get(SYS_OP_RESULT_KEY))
                .operator(systemAttribute.get(SYS_OP_OPERATOR_KEY))
                .ipAddr(systemAttribute.get(SYS_OP_IPADDR_KEY));
        if (StringUtils.equals(systemAttribute.get(SYS_OP_RESULT_KEY), SYS_OP_SUCCEED)) {
            return builder.details(msgGetter.get(SUCCESS_DETAIL_KEY, params.getUserAttribute())).build();
        } else {
            return builder.details(msgGetter.get(FAIL_DETAIL_KEY, params.getUserAttribute())).build();
        }
    }

    static class MessageGetter {
        private static final String DELIMITER = ".";

        private final Plugin plugin;
        private final String baseKey;
        private final Locale locale;

        /**
         * MessageGetter 构造函数。
         *
         * @param plugin 表示插件的{@link Plugin}。
         * @param baseKey 表示资源中的键值的{@link String}。
         * @param locale 表示使用的国际化的资源的{@link Locale}。
         */
        MessageGetter(Plugin plugin, String baseKey, Locale locale) {
            this.plugin = plugin;
            this.baseKey = baseKey;
            this.locale = locale;
        }

        String get() {
            return this.plugin.sr().getMessage(locale, baseKey);
        }

        String get(String subKey) {
            String path = StringUtils.isBlank(subKey) ? baseKey : baseKey + DELIMITER + subKey;
            return this.plugin.sr().getMessage(locale, path);
        }

        String get(String subKey, Map<String, String> params) {
            String template = this.plugin.sr().getMessage(locale, baseKey + DELIMITER + subKey);
            return new DefaultStringTemplate(template).render(params);
        }
    }
}
