package com.cloudmall.outbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明方法在事务提交前写入 Outbox 事件。
 * <p>
 * 单事件模式填写 {@link #eventType()} 与 {@link #payload()}；
 * 多事件模式留空，由方法内部调用 {@code OutboxEvents.collect} 收集。
 * 事务由切面通过 {@code TransactionTemplate} 持有，业务写入与事件写入同生共死。
 * <p>
 * 注意：方法必须经 Spring 代理调用；同一调用链中不要嵌套使用本注解。
 *
 * @author : Tomatos
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutboxEvent {

    /** 事件类型，建议带域前缀，如 FILE_BIND；留空表示由收集器提供 */
    String eventType() default "";

    /** 业务 ID，SpEL 表达式，可为空 */
    String bizId() default "";

    /** 事件载荷，SpEL 表达式；字符串原样写入，其余序列化为 JSON */
    String payload() default "#return";

    /** 写入条件，SpEL 表达式；为空表示始终写入 */
    String condition() default "";

    /** 是否要求本次调用至少产生一条事件 */
    boolean required() default true;
}
