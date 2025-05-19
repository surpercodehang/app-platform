/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.exporter;

import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYSTEM_ATTRIBUTE_EVENT_NAME;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_FAILED;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_RESULT_KEY;
import static modelengine.jade.carver.operation.enums.OperationLogConstant.SYS_OP_SUCCEED;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.jade.carver.operation.support.CompositParam;
import modelengine.jade.service.CarverSpanExporter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户操作导出器。
 *
 * @author 刘信宏
 * @since 2024-07-22
 */
@Component
public class OperationSpanExporter implements CarverSpanExporter {
    private static final Logger log = Logger.get(OperationSpanExporter.class);
    private static final String EXCEPTION_EVENT_NAME = "exception";
    private static final String EXCEPTION_EVENT_MSG_KEY = "exception.message";
    private static final String OPERATION_PREFIX = "operation";

    private final OperationLogExporter logExporter;

    /**
     * 使用操作日志导出器初始化 {@link OperationSpanExporter}。
     *
     * @param logExporter 表示操作日志导出器的 {@link OperationLogExporter}。
     */
    public OperationSpanExporter(OperationLogExporter logExporter) {
        this.logExporter = Validation.notNull(logExporter, "The log exporter cannot be null.");
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spanDataCollection) {
        if (CollectionUtils.isEmpty(spanDataCollection)) {
            return CompletableResultCode.ofSuccess();
        }
        try {
            spanDataCollection.stream()
                    .filter(Objects::nonNull)
                    .filter(span -> span.getName() != null && span.getName().startsWith(OPERATION_PREFIX))
                    .forEach(this::exportSpanData);
            return CompletableResultCode.ofSuccess();
        } catch (Exception exception) {
            log.warn("Export span failed.");
            return CompletableResultCode.ofFailure();
        }
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public String name() {
        return "operation.exporter";
    }

    private void exporterHandle(SpanData spanData) {
        Optional<String> exceptionMessage = this.getExceptionMessage(spanData);
        Map<String, String> systemAttributes = this.getSystemEventAttribute(spanData);
        if (systemAttributes.isEmpty()) {
            log.warn("No operation system event found.");
            return;
        }
        if (exceptionMessage.isPresent()) {
            this.exportFailDetail(spanData, exceptionMessage.get(), systemAttributes);
        } else {
            this.exportSuccessDetail(spanData, systemAttributes);
        }
    }

    private void exportSpanData(SpanData spanData) {
        try {
            this.exporterHandle(spanData);
        } catch (Exception exception) {
            log.warn("Operation export failed. [operation={}]", spanData.getName());
            throw exception;
        }
    }

    private Map<String, String> getSystemEventAttribute(SpanData spanData) {
        return spanData.getEvents()
                .stream()
                .filter(event -> Objects.equals(SYSTEM_ATTRIBUTE_EVENT_NAME, event.getName()))
                .findFirst()
                .map(EventData::getAttributes)
                .map(this::convertAttributesToMap)
                .orElseGet(Collections::emptyMap);
    }

    private Optional<String> getExceptionMessage(SpanData spanData) {
        return spanData.getEvents()
                .stream()
                .filter(event -> Objects.equals(EXCEPTION_EVENT_NAME, event.getName()))
                .findFirst()
                .map(event -> event.getAttributes().get(AttributeKey.stringKey(EXCEPTION_EVENT_MSG_KEY)));
    }

    private void exportFailDetail(SpanData spanData, String errorMessage, Map<String, String> systemAttribute) {
        Map<String, String> userAttributeOnFail =
                MapBuilder.<String, String>get().put(OperationLogExporter.EXCEPTION_DETAIL_KEY, errorMessage).build();
        systemAttribute.put(SYS_OP_RESULT_KEY, SYS_OP_FAILED);
        this.logExporter.export(spanData.getName(), new CompositParam(userAttributeOnFail, systemAttribute));
    }

    private void exportSuccessDetail(SpanData spanData, Map<String, String> systemAttribute) {
        Map<String, String> userAttributesOnSucceed = this.convertAttributesToMap(spanData.getAttributes());
        systemAttribute.put(SYS_OP_RESULT_KEY, SYS_OP_SUCCEED);
        this.logExporter.export(spanData.getName(), new CompositParam(userAttributesOnSucceed, systemAttribute));
    }

    private Map<String, String> convertAttributesToMap(Attributes attributes) {
        return attributes.asMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(entry -> entry.getKey().getKey(), entry -> entry.getValue().toString()));
    }
}
