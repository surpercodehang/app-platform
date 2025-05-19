/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.test.annotation.Mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

/**
 * {@link OperationSpanExporter} 的测试。
 *
 * @author 刘信宏
 * @since 2024-07-25
 */
@FitTestWithJunit(includeClasses = {OperationSpanExporter.class})
public class OperationSpanExporterTest {
    private static final String STUB_SPAN_NAME = "operation.stub";
    private static final String STUB_ATTRIBUTE_KEY = "stubAttributeKey";
    private static final String STUB_ATTRIBUTE_VALUE = "stubAttributeValue";
    private static final String EXCEPTION_EVENT_NAME = "exception";
    private static final String EXCEPTION_EVENT_MSG_KEY = "exception.message";
    private static final String SYSTEM_ATTRIBUTE_EVENT_NAME = "system_attribute";
    private static final String SYS_OP_RESULT_KEY = "sys_operation_result_key";
    private static final String SYS_OP_SUCCEED = "SUCCESS";
    private static final String SYS_OP_FAILED = "FAILED";

    @Fit
    private OperationSpanExporter spanExporter;
    @Mock
    private OperationLogExporter logExporter;
    @Mock
    private SpanData spanData;

    @AfterEach
    void tearDown() {
        clearInvocations(this.logExporter);
    }

    private void mockSucceedSpanData() {
        Mockito.when(this.spanData.getName()).thenReturn(STUB_SPAN_NAME);
        Mockito.when(this.spanData.getEvents())
                .thenReturn(Collections.singletonList(EventData.create(0,
                        SYSTEM_ATTRIBUTE_EVENT_NAME,
                        Attributes.of(AttributeKey.stringKey(EXCEPTION_EVENT_MSG_KEY), STUB_ATTRIBUTE_VALUE))));
        Mockito.when(this.spanData.getAttributes())
                .thenReturn(Attributes.of(AttributeKey.stringKey(STUB_ATTRIBUTE_KEY), STUB_ATTRIBUTE_VALUE));
    }

    private void mockFailedSpanData() {
        Mockito.when(this.spanData.getName()).thenReturn(STUB_SPAN_NAME);
        Mockito.when(this.spanData.getEvents())
                .thenReturn(Arrays.asList(EventData.create(0,
                                EXCEPTION_EVENT_NAME,
                                Attributes.of(AttributeKey.stringKey(EXCEPTION_EVENT_MSG_KEY), STUB_ATTRIBUTE_VALUE)),
                        EventData.create(0,
                                SYSTEM_ATTRIBUTE_EVENT_NAME,
                                Attributes.of(AttributeKey.stringKey(EXCEPTION_EVENT_MSG_KEY), STUB_ATTRIBUTE_VALUE))));
    }

    @Test
    @DisplayName("导出成功操作的操作日志。")
    public void shouldOkWhenExportSucceedOperationLog() {
        this.mockSucceedSpanData();

        this.spanExporter.export(Collections.singletonList(this.spanData));
        verify(this.logExporter).export(eq(STUB_SPAN_NAME), argThat(map -> {
            assertThat(map.getUserAttribute().size()).isEqualTo(1);
            assertThat(map.getUserAttribute()).containsKey(STUB_ATTRIBUTE_KEY);
            assertThat(map.getSystemAttribute()).containsKey(SYS_OP_RESULT_KEY);
            assertThat(map.getSystemAttribute().get(SYS_OP_RESULT_KEY)).isEqualTo(SYS_OP_SUCCEED);
            return true;
        }));
    }

    @Test
    @DisplayName("导出失败操作的操作日志。")
    public void shouldOkWhenExportFailedOperationLog() {
        this.mockFailedSpanData();

        this.spanExporter.export(Collections.singletonList(this.spanData));
        verify(this.logExporter).export(eq(STUB_SPAN_NAME), argThat(map -> {
            assertThat(map.getUserAttribute().size()).isEqualTo(1);
            assertThat(map.getUserAttribute()).containsKey(OperationLogExporter.EXCEPTION_DETAIL_KEY);
            assertThat(map.getSystemAttribute()).containsKey(SYS_OP_RESULT_KEY);
            assertThat(map.getSystemAttribute().get(SYS_OP_RESULT_KEY)).isEqualTo(SYS_OP_FAILED);
            return true;
        }));
    }

    @Test
    @DisplayName("导出日志抛出异常符合预期。")
    public void shouldThrowExceptionWhenExportOperationLog() {
        this.mockSucceedSpanData();
        doAnswer(invocation -> {
            throw new IllegalStateException("test illegal state exception");
        }).when(this.logExporter).export(any(), any());
        assertThat(this.spanExporter.export(Collections.singletonList(this.spanData))).isEqualTo(CompletableResultCode.ofFailure());
    }
}
