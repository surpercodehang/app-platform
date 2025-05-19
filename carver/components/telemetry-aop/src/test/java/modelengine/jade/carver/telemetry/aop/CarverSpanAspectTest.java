/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.carver.telemetry.aop;

import static modelengine.jade.carver.telemetry.aop.stub.SpanDemo.EXCEPTION_MESSAGE;
import static modelengine.jade.carver.telemetry.aop.stub.SpanDemo.PARENT_SPAN_NAME;
import static modelengine.jade.carver.telemetry.aop.stub.SpanDemo.SPAN_ATTRIBUTE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.test.annotation.Mock;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.jade.authentication.context.UserContext;
import modelengine.jade.authentication.context.UserContextHolder;
import modelengine.jade.carver.telemetry.aop.observers.ParamSpanAttributeInjector;
import modelengine.jade.carver.telemetry.aop.observers.ThreadLocalSpanEventInjector;
import modelengine.jade.carver.telemetry.aop.parsers.ComplexSpanAttrParser;
import modelengine.jade.carver.telemetry.aop.parsers.DefaultSpanAttrParser;
import modelengine.jade.carver.telemetry.aop.stub.CarverSpanObjectParse;
import modelengine.jade.carver.telemetry.aop.stub.CarverSpanParserDemo;
import modelengine.jade.carver.telemetry.aop.stub.NestedSpanTestService;
import modelengine.jade.carver.telemetry.aop.stub.NestedSpanTestServiceImpl;
import modelengine.jade.carver.telemetry.aop.stub.SpanDemo;
import modelengine.jade.common.localemessage.ExceptionLocaleService;
import modelengine.jade.service.CarverGlobalOpenTelemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

/**
 * {@link CarverSpanAspect} 的测试。
 *
 * @author 刘信宏
 * @since 2024-07-25
 */
@FitTestWithJunit(includeClasses = {
        CarverSpanAspect.class, SpanDemo.class, NestedSpanTestServiceImpl.class, SpanAttrParserRepository.class,
        DefaultSpanAttrParser.class, ComplexSpanAttrParser.class, CarverSpanParserDemo.class,
        SpanEndObserverRepository.class, ParamSpanAttributeInjector.class, ThreadLocalSpanEventInjector.class
})
public class CarverSpanAspectTest {
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

    @Fit
    private CarverSpanParserDemo carverSpanParserDemo;

    private MockedStatic<CarverGlobalOpenTelemetry> telemetryScopedMock;

    private MockedStatic<UserContextHolder> userContextHolderMock;

    @BeforeEach
    void setup() {
        this.telemetryScopedMock = mockStatic(CarverGlobalOpenTelemetry.class);
        this.telemetryScopedMock.when(CarverGlobalOpenTelemetry::get).thenReturn(this.mockOpenTelemetry);
        this.userContextHolderMock = mockStatic(UserContextHolder.class);
        this.userContextHolderMock.when(UserContextHolder::get).thenReturn(new UserContext("Admin", "127.0.0.1", "en"));
        when(this.mockOpenTelemetry.getTracer(any())).thenReturn(this.mockTrace);
        when(this.mockTrace.spanBuilder(any())).thenReturn(this.mockSpanBuilder);
        when(this.mockSpanBuilder.startSpan()).thenReturn(this.mockSpan);
        doAnswer(args -> ObjectUtils.<Throwable>cast(args.getArgument(0))
                .getMessage()).when(this.exceptionLocaleService).localizeMessage(any());
    }

    @AfterEach
    void tearDown() {
        this.telemetryScopedMock.close();
        this.userContextHolderMock.close();
        clearInvocations(this.mockOpenTelemetry, this.mockTrace, this.mockSpanBuilder, this.mockSpan);
    }

    @Test
    @DisplayName("触发切面，成功设置 Span 属性。")
    void shouldOkWhenHandleSuccessThenSetSpanAttr() {
        String playerArg = "playerArg";
        this.spanDemo.handleSuccess(playerArg);

        verify(this.mockSpan).setAttribute(eq(SPAN_ATTRIBUTE_KEY), eq(playerArg));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("触发切面，业务异常，成功设置异常 Span 属性。")
    void shouldOkWhenHandleExceptionThenSetSpanEvent() {
        String playerArg = "playerArg";
        assertThatThrownBy(() -> this.spanDemo.handleException(playerArg)).isInstanceOf(IllegalStateException.class);

        verify(this.mockSpan).recordException(argThat(throwable -> {
            assertThat(throwable.getMessage()).isEqualTo(playerArg + EXCEPTION_MESSAGE);
            return true;
        }));
        verify(this.mockSpan).setStatus(eq(StatusCode.ERROR), eq(playerArg + EXCEPTION_MESSAGE));
        verify(this.mockSpan).setAttribute(eq(SPAN_ATTRIBUTE_KEY), eq(playerArg));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("触发嵌套切面，成功设置 Span 属性。")
    void shouldOkWhenHandleNestedSpanThenSetSpanEvent() {
        doAnswer(arg -> {
            Context argument = ObjectUtils.cast(arg.getArgument(0));
            return argument.with(ContextKey.named("opentelemetry-trace-span-key"), this.mockSpan);
        }).when(this.mockSpan).storeInContext(any(Context.class));

        String playerArg = "playerArg";
        this.spanDemo.handleNested(playerArg);

        verify(this.mockSpan, times(1)).makeCurrent();
        verify(this.mockTrace).spanBuilder(eq(PARENT_SPAN_NAME));
        verify(this.mockTrace).spanBuilder(eq(NestedSpanTestService.NESTED_SPAN_NAME));
        verify(this.mockSpan).setAttribute(eq(SPAN_ATTRIBUTE_KEY), eq(playerArg));
        verify(this.mockSpan).setAttribute(eq(NestedSpanTestService.NESTED_ATTR_KEY), eq(playerArg));
        verify(this.mockSpan, times(2)).end();
    }

    @Test
    @DisplayName("触发解析器解析基本类型，成功设置 Span 属性。")
    void shouldOkWhenMatchPrimitiveExpression() {
        Object obj = 10;
        this.carverSpanParserDemo.handlePrimitiveParser(obj);
        verify(this.mockSpan).setAttribute(eq("player"), eq(""));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("触发解析器解析键值对，成功设置 Span 属性。")
    void shouldOkWhenMatchKVExpression() {
        Object kvObj = MapBuilder.<String, Object>get()
                .put("k1", MapBuilder.<String, Object>get().put("k2", "v").build())
                .put("k11", MapBuilder.<String, Object>get().put("k22", "v").build())
                .build();
        this.carverSpanParserDemo.handleKVParser(kvObj);
        verify(this.mockSpan).setAttribute(eq("player"), eq("v"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v"));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("触发解析器解析对象，成功设置 Span 属性。")
    void shouldOkWhenMatchObjectExpression() {
        Object obj = new CarverSpanObjectParse.Outer(new CarverSpanObjectParse.Inner("v"));
        this.carverSpanParserDemo.handleObjectParser(obj);
        verify(this.mockSpan).setAttribute(eq("player"), eq("{k2=v}"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v"));
        verify(this.mockSpan).setAttribute(eq("player3"), eq("v"));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("触发解析器解析列表，成功设置 Span 属性。")
    void shouldOkWhenMatchListExpression() {
        Object outers =
                Collections.singletonList(new CarverSpanObjectParse.Outer(new CarverSpanObjectParse.Inner("v1")));
        this.carverSpanParserDemo.handleListParser(outers);
        verify(this.mockSpan).setAttribute(eq("player"), eq("{k2=v1}"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v1"));
        verify(this.mockSpan).setAttribute(eq("player3"), eq(""));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("属性数组: 触发解析器解析数组对，成功设置 Span 属性。")
    void shouldOkWhenMatchMultiKVExpression() {
        Object kvObj = MapBuilder.<String, Object>get()
                .put("k1", MapBuilder.<String, Object>get().put("k2", "v").build())
                .put("k11", MapBuilder.<String, Object>get().put("k22", "v").build())
                .build();
        this.carverSpanParserDemo.handleMultiKVParser(kvObj);
        verify(this.mockSpan).setAttribute(eq("player"), eq("v"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v"));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("属性数组: 触发解析器解析对象，成功设置 Span 属性。")
    void shouldOkWhenMatchMultiObjectExpression() {
        Object obj = new CarverSpanObjectParse.Outer(new CarverSpanObjectParse.Inner("v"));
        this.carverSpanParserDemo.handleMultiObjectParser(obj);
        verify(this.mockSpan, times(2)).setAttribute(eq("player"), eq("{k2=v}"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v"));
        verify(this.mockSpan).setAttribute(eq("player3"), eq("v"));
        verify(this.mockSpan).setAttribute(eq("player4"), eq("{k1={k2=v}}"));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("属性数组: 触发解析器解析列表，成功设置 Span 属性。")
    void shouldOkWhenMatchMultiListExpression() {
        Object outers =
                Collections.singletonList(new CarverSpanObjectParse.Outer(new CarverSpanObjectParse.Inner("v1")));
        this.carverSpanParserDemo.handleMultiListParser(outers);
        verify(this.mockSpan).setAttribute(eq("player"), eq("{k2=v1}"));
        verify(this.mockSpan).setAttribute(eq("player2"), eq("v1"));
        verify(this.mockSpan).setAttribute(eq("player3"), eq(""));
        verify(this.mockSpan).end();
    }

    @Test
    @DisplayName("属性数组: 数组为空，失败设置 Span 属性。")
    void shouldFailWhenMatchEmptyArrayExpression() {
        Object obj = new CarverSpanObjectParse.Outer(new CarverSpanObjectParse.Inner("v"));
        this.carverSpanParserDemo.handleEmptyArrayParser(obj);
        verify(this.mockSpan, times(0)).setAttribute(any(String.class), any(String.class));
        verify(this.mockSpan).end();
    }
}
