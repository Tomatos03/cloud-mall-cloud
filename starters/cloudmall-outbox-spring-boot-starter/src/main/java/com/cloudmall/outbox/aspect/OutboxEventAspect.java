package com.cloudmall.outbox.aspect;

import java.lang.reflect.Method;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.support.TransactionTemplate;

import com.cloudmall.outbox.annotation.OutboxEvent;
import com.cloudmall.outbox.appender.OutboxAppender;
import com.cloudmall.outbox.collector.OutboxEvents;

/**
 * Outbox 注解切面。
 * <p>
 * 通过 {@link TransactionTemplate} 持有事务边界：无论事务拦截器位于切面内外，
 * 业务写入与事件写入都处于同一事务；业务异常时不产生事件。
 *
 * @author : Tomatos
 */
@Aspect
@RequiredArgsConstructor
public class OutboxEventAspect {

    private final OutboxAppender outboxAppender;
    private final TransactionTemplate transactionTemplate;
    private final BeanFactory beanFactory;
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 环绕被注解方法：事务内执行并写入事件。
     *
     * @param joinPoint 连接点
     * @param ann 注解
     * @return 方法返回值
     * @throws Throwable 方法异常原样抛出
     */
    @Around("@annotation(ann)")
    public Object around(ProceedingJoinPoint joinPoint, OutboxEvent ann) throws Throwable {
        int mark = OutboxEvents.mark();
        try {
            return transactionTemplate.execute(
                    status -> {
                        try {
                            Object result = joinPoint.proceed();
                            flush(ann, joinPoint, result, mark);
                            return result;
                        } catch (Throwable t) {
                            throw new InvocationFailure(t);
                        }
                    }
            );
        } catch (InvocationFailure e) {
            throw e.getCause();
        } finally {
            OutboxEvents.discardFrom(mark);
        }
    }

    /**
     * 按注解配置写入事件。
     *
     * @param ann 注解
     * @param joinPoint 连接点
     * @param result 方法返回值
     * @param mark 收集游标
     */
    private void flush(OutboxEvent ann, ProceedingJoinPoint joinPoint, Object result, int mark) {
        if (!matches(ann.condition(), joinPoint, result)) {
            return;
        }
        if (StrUtil.isNotBlank(ann.eventType())) {
            outboxAppender.append(
                    ann.eventType(),
                    asText(evaluate(ann.bizId(), joinPoint, result)),
                    evaluate(ann.payload(), joinPoint, result)
            );
            return;
        }
        List<OutboxEvents.Event> events = OutboxEvents.drainFrom(mark);
        if (CollUtil.isEmpty(events)) {
            if (ann.required()) {
                throw new IllegalStateException(
                        "@OutboxEvent 未产生任何事件: " + joinPoint.getSignature().toShortString()
                );
            }
            return;
        }
        for (OutboxEvents.Event event : events) {
            outboxAppender.append(event.eventType(), event.bizId(), event.payload());
        }
    }

    /**
     * 判断条件表达式是否成立。
     *
     * @param condition 条件表达式
     * @param joinPoint 连接点
     * @param result 方法返回值
     * @return true 表示需要写入
     */
    private boolean matches(String condition, ProceedingJoinPoint joinPoint, Object result) {
        if (StrUtil.isBlank(condition)) {
            return true;
        }
        return Boolean.TRUE.equals(evaluate(condition, joinPoint, result));
    }

    /**
     * 求值 SpEL 表达式。
     *
     * @param expression 表达式，为空返回 null
     * @param joinPoint 连接点
     * @param result 方法返回值
     * @return 求值结果
     */
    private Object evaluate(String expression, ProceedingJoinPoint joinPoint, Object result) {
        if (StrUtil.isBlank(expression)) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext(joinPoint.getTarget());
        registerParameters(context, method, args);
        context.setVariable("return", result);
        context.setVariable("result", result);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return spelExpressionParser.parseExpression(expression).getValue(context);
    }

    /**
     * 将方法参数注册为 SpEL 变量：参数名、位置别名 pN / aN。
     *
     * @param context SpEL 上下文
     * @param method 方法
     * @param args 参数值
     */
    private void registerParameters(StandardEvaluationContext context, Method method, Object[] args) {
        String[] names = parameterNameDiscoverer.getParameterNames(method);
        if (names != null) {
            for (int i = 0; i < names.length && i < args.length; i++) {
                context.setVariable(names[i], args[i]);
            }
        }
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }
    }

    /**
     * 转为文本值。
     *
     * @param value 值
     * @return 文本，null 保持 null
     */
    private static String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 包装受检异常，用于穿越事务回调签名。
     */
    private static class InvocationFailure extends RuntimeException {

        InvocationFailure(Throwable cause) {
            super(cause);
        }
    }
}
