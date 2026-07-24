package com.cloudmall.aigc.constant;

/**
 * 聊天回复占位符模板常量。
 * <p>
 * 模板中的 {userInput} 占位符由前端替换为用户实际输入后提交。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
public interface ChatTemplates {

    /**
     * 润色 — 优化语法和表达，保持原意不变
     */
    String POLISH = """
            请润色以下文本，优化语法和表达：
            ```
            {userInput}
            ```
            保持原意不变，仅优化语言表达。
            """;

    /**
     * 续写 — 基于已有内容续写，保持风格一致
     */
    String CONTINUE = """
            请基于以下内容续写，保持风格一致：
            ```
            {userInput}
            ```
            请自然地继续写下去。
            """;

    /**
     * 帮写 — 根据描述生成完整、通顺的文本
     */
    String HELP_WRITE = """
            请根据以下要求撰写内容：
            ```
            {userInput}
            ```
            请生成完整、通顺的文本。
            """;
}
