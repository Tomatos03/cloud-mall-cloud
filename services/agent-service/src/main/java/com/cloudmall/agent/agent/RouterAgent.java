package com.cloudmall.agent.agent;

import com.cloudmall.agent.model.req.ChatReq;
import com.cloudmall.agent.model.resp.ChatResp;
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
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个电商平台的路由助手。分析用户的问题，
            从以下智能体中选择最合适的一个来回答。

            %s

            返回 JSON 格式(不要加 markdown 代码块标记), 示例：
            {"agentType":"goods", "confidence":"high", "reason":"用户询问商品价格"}

            要求：
            - agentType 必须是上面列出的小写名称之一
            - 如果无法确定，选择 general
            """;

    private final ChatClient routerChatClient;
    private final Map<AgentType, Agent> agentMap;

    public RouterAgent(
            ChatClient.Builder clientBuilder,
            List<Agent> agents
    ) {
        this.agentMap = agents.stream()
                              .filter(agent -> AgentType.ROUTE != agent.getType())
                              .collect(Collectors.toMap(Agent::getType, Function.identity()));

        String agentDescriptions = agentMap
                .values()
                .stream()
                .map(this::formatAgentDescription)
                .collect(Collectors.joining("\n"));
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, agentDescriptions);

        if (log.isDebugEnabled()) {
            log.debug("路由 system prompt:\n{}", systemPrompt);
        }

        this.routerChatClient = clientBuilder.defaultSystem(systemPrompt)
                                             .build();
    }

    @Override
    public ChatClient getChatClient() {
        return routerChatClient;
    }

    @Override
    public AgentType getType() {
        return AgentType.ROUTE;
    }

    @Override
    public String getDescription() {
        return "分析用户意图并路由到合适的业务智能体";
    }

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
