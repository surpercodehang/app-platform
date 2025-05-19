/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.test.annotation.Mock;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.jade.carver.telemetry.aop.observers.ParamSpanAttributeInjector;
import modelengine.jade.carver.telemetry.aop.parsers.DefaultSpanAttrParser;
import modelengine.jade.carver.telemetry.aop.stub.NestedAppendSpanTestImpl;
import modelengine.jade.carver.telemetry.aop.stub.NestedSpanTestService;
import modelengine.jade.carver.telemetry.aop.stub.SpanDemo;
import modelengine.jade.common.localemessage.ExceptionLocaleService;
import modelengine.jade.service.CarverGlobalOpenTelemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link AppendSpanAttrAspect} 的测试。
 *
 * @author 马朝阳
 * @since 2024-08-05
 */
@FitTestWithJunit(includeClasses = {
        SpanDemo.class, AppendSpanAttrAspect.class, NestedAppendSpanTestImpl.class, NestedAppendSpanTestImpl.class,
        CarverSpanAspect.class, SpanAttrParserRepository.class, DefaultSpanAttrParser.class,
        SpanEndObserverRepository.class, ParamSpanAttributeInjector.class
})
public class AppendSpanAttrAspectTest {
    @Mock
    private OpenTelemetry mockOpenTelemetry;
    @Mock
    private Tracer mockTrace;
    @Mock
    private SpanBuilder mockSpanBuilder;
    @Mock
    private Span mockSpan;
    @Mock
    private ExceptionLocaleService exceptionLocaleService;
    @Fit
    private SpanDemo spanDemo;
    private MockedStatic<CarverGlobalOpenTelemetry> telemetryScopedMock;
    private MockedStatic<Span> mockCurrent;

    @BeforeEach
    void setup() {
        this.telemetryScopedMock = mockStatic(CarverGlobalOpenTelemetry.class);
        this.mockCurrent = mockStatic(Span.class);
        this.telemetryScopedMock.when(CarverGlobalOpenTelemetry::get).thenReturn(this.mockOpenTelemetry);
        when(this.mockOpenTelemetry.getTracer(any())).thenReturn(this.mockTrace);
        when(this.mockTrace.spanBuilder(any())).thenReturn(this.mockSpanBuilder);
        when(this.mockSpanBuilder.startSpan()).thenReturn(this.mockSpan);
        this.mockCurrent.when(Span::current).thenReturn(this.mockSpan);
        doAnswer(args -> ObjectUtils.<Throwable>cast(args.getArgument(0))
                .getMessage()).when(this.exceptionLocaleService).localizeMessage(any());
    }

    @AfterEach
    void tearDown() {
        this.telemetryScopedMock.close();
        this.mockCurrent.close();
        clearInvocations(this.mockOpenTelemetry, this.mockTrace, this.mockSpanBuilder, this.mockSpan);
    }

    @Test
    @DisplayName("触发嵌套切面，成功设置 Span 属性。")
    void shouldOkWhenHandleNestedAddingSpanAttributes() {
        String playerArg = "playerArg";
        this.spanDemo.handleNested(playerArg);

        verify(this.mockSpanBuilder, times(1)).startSpan();
        verify(this.mockSpan).setAttribute(eq(SpanDemo.SPAN_ATTRIBUTE_KEY), eq(playerArg));
        verify(this.mockSpan).setAttribute(eq(NestedSpanTestService.NESTED_ATTR_KEY), eq(playerArg));
    }

    @Test
    @DisplayName("触发嵌套切面，业务异常，成功设置异常 Span 属性。")
    void shouldOkWhenHandleExceptionThenSetSpanEvent() {
        String playerArg = "exception";
        assertThatThrownBy(() -> this.spanDemo.handleNested(playerArg)).isInstanceOf(IllegalStateException.class);

        verify(this.mockSpan).recordException(any(Throwable.class));
        verify(this.mockSpanBuilder, times(1)).startSpan();
        verify(this.mockSpan).setStatus(eq(StatusCode.ERROR), any());
        verify(this.mockSpan).setAttribute(eq(SpanDemo.SPAN_ATTRIBUTE_KEY), eq(playerArg));
        verify(this.mockSpan).setAttribute(eq(NestedSpanTestService.NESTED_ATTR_KEY), eq(playerArg));
    }
}