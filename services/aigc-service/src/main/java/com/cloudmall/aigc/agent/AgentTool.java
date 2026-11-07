package com.cloudmall.aigc.agent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 标记一个工具类，并声明该工具可以被哪些智能体使用。
 * <p>
 * 使用示例：
 * <pre>{@code
 *
 * @AgentTool(agents = AgentTypeEnum.PRODUCT)
 * public class ProductSpuTool {
 *     @Tool(description = "根据SPU查询商品详情")
 *     public ProductResp getBySpu(String spu) { ... }
 * }
 *
 * @AgentTool(exclude = AgentTypeEnum.REFUND)
 * public class CartTool { ... }
 *
 * @AgentTool  // 默认所有智能体可用
 * public class CommonTool { ... }
 * }</pre>
 *
 * @see AgentTypeEnum
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentTool {

    /**
     * 指定哪些智能体可以使用该工具。
     * 为空时表示所有智能体都可以使用（配合 exclude 排除）。
     */
    AgentTypeEnum[] agents() default {
    };

    /**
     * 排除哪些智能体不能使用该工具。
     * 优先级高于 agents：exclude 中指定的智能体即使出现在 agents 中也不会获得该工具。
     */
    AgentTypeEnum[] exclude() default {
    };
}
