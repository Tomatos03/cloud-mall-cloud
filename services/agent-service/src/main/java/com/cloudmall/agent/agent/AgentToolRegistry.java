package com.cloudmall.agent.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 按 {@link AgentType} 归类所有带 {@link AgentTool} 注解的工具 Bean。
 * <p>
 * Spring 单例，容器启动时一次性扫描并缓存，供所有 {@link AbstractAgent} 实例共享。
 */
@Component
public class AgentToolRegistry {

    private final Map<AgentType, Object[]> toolMap;

    public AgentToolRegistry(ApplicationContext context) {
        this.toolMap = buildAgentAvailableToolsMap(context);
    }

    /**
     * 获取指定智能体可用的工具数组。
     */
    public Object[] getTools(AgentType type) {
        return toolMap.getOrDefault(type, new Object[0]);
    }

    private static Map<AgentType, Object[]> buildAgentAvailableToolsMap(ApplicationContext context) {
        Map<String, Object> toolBeans = context.getBeansWithAnnotation(AgentTool.class);

        Map<AgentType, ArrayList<Object>> result = Arrays
                .stream(AgentType.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> new ArrayList<>()
                ));

        for (Object tool : toolBeans.values()) {
            AgentTool ann = tool.getClass().getAnnotation(AgentTool.class);

            List<AgentType> list = List.of(ann.agents().length == 0 ? AgentType.values() : ann.agents());
            Set<AgentType> allowed = new HashSet<>(list);
            List.of(ann.exclude()).forEach(allowed::remove);

            for (AgentType type : allowed) {
                result.get(type).add(tool);
            }
        }

        return result.entrySet()
                     .stream()
                     .collect(Collectors.toMap(
                             Map.Entry::getKey,
                             entry -> entry.getValue().toArray()
                     ));
    }
}
