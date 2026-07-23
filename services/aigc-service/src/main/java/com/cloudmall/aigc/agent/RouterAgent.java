package com.cloudmall.aigc.agent;

import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 路由智能体。
 * <p>
 * 不继承 AbstractAgent，因为路由的逻辑与业务智能体完全不同：
 * <ul>
 *   <li>不需要流式生成</li>
 *   <li>不需要工具</li>
 *   <li>不需要 ChatMemory</li>
 * </ul>
 * <p>
 * 职责：分析用户意图 → 路由到合适的业务智能体 → 返回流式结果。
 */
@Slf4j
@Service
public class RouterAgent implements Agent {
    /** Nacos 路由 prompt 模板中的 Agent 描述占位符 */
    public static final String AGENT_DESCRIPTIONS = "agent_descriptions";

    private final AgentPromptLoader promptLoader;
    private final ChatClient.Builder chatClientBuilder;
    private final Map<AgentType, Agent> agentMap;
    private ChatClient routerChatClient;

    /**
     * 构造路由智能体，初始化子智能体映射并构建路由 ChatClient。
     * <p>
     * 自动注册 Nacos 热更新监听，提示词变更时重建路由 ChatClient。
     *
     * @param clientBuilder ChatClient 构建器
     * @param agents        所有业务智能体列表（不含路由自身）
     * @param promptLoader  提示词加载器，用于加载路由 system prompt
     */
    public RouterAgent(
            ChatClient.Builder clientBuilder,
            List<Agent> agents,
            AgentPromptLoader promptLoader
    ) {
        this.chatClientBuilder = clientBuilder;
        this.promptLoader = promptLoader;
        this.agentMap = agents.stream()
                              .filter(agent -> AgentType.ROUTE != agent.getType())
                              .collect(Collectors.toMap(Agent::getType, Function.identity()));

        this.routerChatClient = buildRouterChatClient();

        // 注册 Nacos 热更新监听
        promptLoader.registerListener(AgentType.ROUTE, () -> {
            this.routerChatClient = buildRouterChatClient();
            log.info("RouterAgent ChatClient 已重建（热更新）");
        });
    }

    /**
     * 构建路由 ChatClient：从 Nacos 加载 prompt 模板.
     */
    private ChatClient buildRouterChatClient() {
        String systemPrompt = promptLoader.load(AgentType.ROUTE);
        String agentDescriptions = agentMap.values()
                                           .stream()
                                           .map(this::formatAgentDescription)
                                           .collect(Collectors.joining("\n"));

        if (log.isDebugEnabled()) {
            log.debug("路由 system prompt:\n{}", systemPrompt);
        }

        return chatClientBuilder.defaultSystem(promptSystemSpec -> promptSystemSpec
                                        .text(systemPrompt)
                                        .param(AGENT_DESCRIPTIONS, agentDescriptions)
                                )
                                .build();
    }

    /**
     * 获取路由智能体的 ChatClient 实例。
     *
     * @return 路由 ChatClient 实例
     */
    @Override
    public ChatClient getChatClient() {
        return routerChatClient;
    }

    /**
     * 获取智能体类型。
     *
     * @return {@link AgentType#ROUTE}
     */
    @Override
    public AgentType getType() {
        return AgentType.ROUTE;
    }

    /**
     * 获取路由智能体职责描述。
     *
     * @return 职责描述字符串
     */
    @Override
    public String getDescription() {
        return "分析用户意图并路由到合适的业务智能体";
    }

    /**
     * 处理聊天请求：分析用户意图 → 路由到合适的业务智能体 → 返回流式结果。
     *
     * @param req 聊天请求，包含会话ID和用户提示词
     * @return 流式聊天响应
     */
    @Override
    public Flux<ChatResp> chat(ChatReq req) {
        log.debug("路由请求: sessionId={}, prompt={}", req.getSessionId(), req.getPrompt());

        return Mono.fromCallable(() -> classifyIntent(req.getPrompt()))
                   .flatMapMany(decision -> {
                       Agent agent = agentMap.getOrDefault(decision.agentType(), agentMap.get(AgentType.GENERAL));
                       log.debug("路由决策: sessionId={}, agent={}, confidence={}, reason={}",
                                req.getSessionId(), decision.agentType(),
                                decision.confidence(), decision.reason()
                       );
                       return agent.chat(req);
                   });
    }

    /**
     * 将智能体格式化为路由提示词中的描述行。
     *
     * @param agent 智能体实例
     * @return 格式化后的描述行，如 {@code - goods: 负责商品搜索...}
     */
    private String formatAgentDescription(Agent agent) {
        return "- " + agent.getType().name().toLowerCase() + ": " + agent.getDescription();
    }

    /**
     * 通过轻量 LLM 调用 + 结构化输出进行意图分类。
     *
     * @param prompt 用户输入
     * @return 路由决策结果
     */
    private RouteDecision classifyIntent(String prompt) {
        return routerChatClient.prompt()
                               .user(prompt)
                               .call()
                               .entity(RouteDecision.class);
    }

    /**
     * 路由决策的结构化输出载体。
     */
    public record RouteDecision(AgentType agentType, String confidence, String reason) {}
}
