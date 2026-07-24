package com.cloudmall.aigc.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天回复占位符模板。
 * <p>
 * 前端获取后用于渲染写作辅助模式（润色/续写/帮写），
 * 用户输入内容替换模板中的 {userInput} 占位符后作为请求发送。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTemplate {

    /**
     * 模板类型标识，如 "polish"、"continue"、"help-write"
     */
    private String type;

    /**
     * 展示名称，如 "润色"、"续写"、"帮写"
     */
    private String name;

    /**
     * 含 {userInput} 占位符的 prompt 模板文本
     */
    private String template;
}
