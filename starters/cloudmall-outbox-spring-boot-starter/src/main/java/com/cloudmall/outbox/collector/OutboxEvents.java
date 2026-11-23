package com.cloudmall.outbox.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;

/**
 * Outbox 事件收集器。
 * <p>
 * 多事件场景下由业务方法调用 {@link #collect} 收集事件，切面在方法正常返回后统一写入。
 * 按调用深度记录游标，嵌套调用时只处理本次调用新增的事件。
 *
 * @author : Tomatos
 */
public final class OutboxEvents {

    private static final ThreadLocal<List<Event>> HOLDER = ThreadLocal.withInitial(ArrayList::new);

    private OutboxEvents() {}

    /**
     * 收集一条事件。
     *
     * @param eventType 事件类型
     * @param bizId 业务 ID
     * @param payload 事件载荷，字符串原样写入，其余序列化为 JSON
     */
    public static void collect(String eventType, String bizId, Object payload) {
        HOLDER.get().add(new Event(eventType, bizId, payload));
    }

    /**
     * 批量收集同一事件类型与业务 ID 的事件。
     *
     * @param eventType 事件类型
     * @param bizId 业务 ID
     * @param payloads 事件载荷集合，字符串原样写入，其余序列化为 JSON
     */
    public static void collectAll(String eventType, String bizId, Collection<?> payloads) {
        if (CollUtil.isEmpty(payloads)) {
            return;
        }
        HOLDER.get().addAll(CollStreamUtil.toList(payloads, payload -> new Event(eventType, bizId, payload)));
    }

    /**
     * 记录当前收集游标（框架内部使用）。
     *
     * @return 当前已收集的事件数量
     */
    public static int mark() {
        return HOLDER.get().size();
    }

    /**
     * 取出游标之后的事件并移除（框架内部使用）。
     *
     * @param mark 收集游标
     * @return 本次调用新增的事件
     */
    public static List<Event> drainFrom(int mark) {
        List<Event> all = HOLDER.get();
        List<Event> collected = List.copyOf(all.subList(mark, all.size()));
        clearFrom(mark);
        return collected;
    }

    /**
     * 丢弃游标之后的事件（框架内部使用）。
     *
     * @param mark 收集游标
     */
    public static void discardFrom(int mark) {
        clearFrom(mark);
    }

    private static void clearFrom(int mark) {
        List<Event> all = HOLDER.get();
        if (all.size() > mark) {
            all.subList(mark, all.size()).clear();
        }
        if (all.isEmpty()) {
            HOLDER.remove();
        }
    }

    /**
     * 收集到的事件。
     *
     * @param eventType 事件类型
     * @param bizId 业务 ID
     * @param payload 事件载荷
     */
    public record Event(String eventType, String bizId, Object payload) {
    }
}
