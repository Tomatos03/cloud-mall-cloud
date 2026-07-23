package com.cloudmall.agent.agent;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.cloudmall.agent.properties.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Nacos 提示词懒加载器。
 * <p>
 * 首次路由到某个智能体时才从 Nacos 读取对应的 system prompt 文件，
 * 读取后缓存，后续相同智能体的请求直接走缓存。
 * 同时注册 Nacos Listener 支持热更新。
 */
@Slf4j
@Component
public class AgentPromptLoader {
    private final ConfigService configService;
    private final Map<AgentType, PromptConfig> promptConfigs;
    private final String defaultGroup;
    private final int defaultTimeout;
    private final String globalPrompt;
    private final Map<AgentType, String> cache = new ConcurrentHashMap<>();

    public AgentPromptLoader(NacosConfigManager nacosConfigManager,
                             AgentProperties properties) {
        this.configService = nacosConfigManager.getConfigService();
        this.defaultGroup = properties.getPromptGroup();
        this.defaultTimeout = properties.getReadTimeout();
        this.globalPrompt = properties.getGlobalPrompt();

        Map<String, AgentProperties.PromptConfig> raw = properties.getPrompts();
        if (raw != null) {
            this.promptConfigs = raw.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> AgentType.valueOf(e.getKey().toUpperCase()),
                            e -> {
                                AgentProperties.PromptConfig cfg = e.getValue();
                                PromptConfig pc = new PromptConfig();
                                pc.setDataId(cfg.getDataId());
                                pc.setGroup(cfg.getGroup());
                                pc.setReadTimeout(cfg.getReadTimeout());
                                return pc;
                            }
                    ));
        } else {
            this.promptConfigs = Map.of();
        }
    }
    
    /**
     * 懒加载：首次加载从 Nacos 读取，之后走缓存。
     * 返回的提示词已包含 globalPrompt + 专属 prompt。
     */
    public String load(AgentType type) {
        boolean cached = cache.containsKey(type);
        String agentPrompt = cache.computeIfAbsent(type, this::loadFromNacos);
        // globalPrompt 不缓存，热更新时无需重建
        String fullPrompt = combineWithGlobal(agentPrompt);
        return fullPrompt;
    }

    /**
     * 从 Nacos 读取指定智能体的提示词。
     * 读取成功返回内容并缓存，失败返回默认提示词。
     *
     * @param type 智能体类型
     * @return 提示词内容，读取失败时返回 {@link #DEFAULT_PROMPT}
     */
    private String loadFromNacos(AgentType type) {
        PromptConfig cfg = promptConfigs.get(type);
        if (cfg == null || cfg.getDataId() == null || cfg.getDataId().isBlank()) {
            log.warn("智能体未配置 Nacos dataId: type={}", type);
            return DEFAULT_PROMPT;
        }

        String dataId = cfg.getDataId();
        String group = cfg.getGroup() != null ? cfg.getGroup() : defaultGroup;
        int timeout = cfg.getReadTimeout() != null ? cfg.getReadTimeout() : defaultTimeout;

        try {
            String content = configService.getConfig(dataId, group, timeout);
            if (content == null || content.isEmpty()) {
                log.warn("Nacos 提示词为空: type={}, dataId={}, group={}", type, dataId, group);
                return DEFAULT_PROMPT;
            }
            log.info("加载提示词成功: type={}, dataId={}, group={}", type, dataId, group);
            if (log.isDebugEnabled()) {
                log.debug("Nacos 提示词内容 (type={}):\n{}", type, content);
            }
            return content;
        } catch (NacosException e) {
            log.error("加载提示词失败: type={}, dataId={}, group={}", type, dataId, group, e);
            return DEFAULT_PROMPT;
        }
    }

    /**
     * 为指定智能体注册 Nacos 配置变更监听，支持热更新。
     *
     * @param type     智能体类型
     * @param onUpdate 配置变更后的回调
     */
    public void registerListener(AgentType type, Runnable onUpdate) {
        PromptConfig cfg = promptConfigs.get(type);
        if (cfg == null || cfg.getDataId() == null || cfg.getDataId().isBlank()) {
            log.warn("智能体未配置 Nacos dataId，跳过监听注册: type={}", type);
            return;
        }

        String dataId = cfg.getDataId();
        String group = cfg.getGroup() != null ? cfg.getGroup() : defaultGroup;

        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String config) {
                    cache.put(type, config);
                    log.info("提示词热更新: type={}, dataId={}", type, dataId);
                    onUpdate.run();
                }

                @Override
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor();
                }
            });
        } catch (NacosException e) {
            log.error("注册 Nacos 监听失败: dataId={}", dataId, e);
        }
    }

    /**
     * 合并 global prompt 和 Agent 专属 prompt。
     * globalPrompt 为空时只返回 agentPrompt。
     *
     * @param agentPrompt 智能体专属 prompt
     * @return 合并后的完整 prompt
     */
    private String combineWithGlobal(String agentPrompt) {
        if (globalPrompt == null || globalPrompt.isBlank()) {
            return agentPrompt;
        }
        return globalPrompt + "\n\n" + agentPrompt;
    }

    private static final String DEFAULT_PROMPT = "你是一个智能客服助手，请礼貌、准确地回答用户的问题。";

    /**
     * 内部配置载体
     */
    public static class PromptConfig {
        private String dataId;
        private String group;
        private Integer readTimeout;

        public String getDataId() { return dataId; }
        public void setDataId(String dataId) { this.dataId = dataId; }
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public Integer getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Integer readTimeout) { this.readTimeout = readTimeout; }
    }
}