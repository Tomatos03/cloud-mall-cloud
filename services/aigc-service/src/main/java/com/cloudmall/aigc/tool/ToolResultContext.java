package com.cloudmall.aigc.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.hutool.core.collection.CollUtil;

import com.cloudmall.aigc.model.resp.AgentCardResp;

/**
 * 工具结果上下文
 * <p>
 * 用于在工具调用过程中存储结果，通过 ToolContext 传递给工具。
 * 工具可以将结果 put 到此上下文中，流式响应完成后统一发送 PARAM 事件。
 * </p>
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
public class ToolResultContext {

    private final Map<String, Object> results = new ConcurrentHashMap<>();
    private final List<AgentCardResp> cards = new CopyOnWriteArrayList<>();

    /**
     * 将工具结果放入上下文 <p>
     * key 为结果对象的简单类名，value 为整个对象 </p>
     *
     * @param key 结果对象的简单类名
     * @param value 结果对象
     */
    public void put(String key, Object value) {
        results.put(key, value);
    }

    /**
     * 追加前端卡片结果。
     *
     * @param cards 卡片列表
     */
    public void addCards(List<AgentCardResp> cards) {
        if (CollUtil.isEmpty(cards)) {
            return;
        }
        this.cards.addAll(cards);
    }

    /**
     * 获取所有卡片结果。
     *
     * @return 卡片列表
     */
    public List<AgentCardResp> getCards() {
        return List.copyOf(cards);
    }

    /**
     * 获取所有工具结果
     *
     * @return 包含所有结果的 Map，key 为简单类名，value 为对象
     */
    public Map<String, Object> getResults() {
        Map<String, Object> all = new HashMap<>(results);
        if (CollUtil.isNotEmpty(cards)) {
            all.put("cards", List.copyOf(cards));
        }
        return all;
    }

    /**
     * 判断是否有工具结果
     *
     * @return true 表示有结果，false 表示无结果
     */
    public boolean hasResults() {
        return !results.isEmpty() || CollUtil.isNotEmpty(cards);
    }
}
