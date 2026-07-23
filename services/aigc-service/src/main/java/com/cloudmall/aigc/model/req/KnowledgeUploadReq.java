package com.cloudmall.aigc.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 知识库文档上传请求
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeUploadReq {

    /** 文档内容 */
    @NotBlank(message = "文档内容不能为空")
    private String content;

    /** 文档元数据 */
    private Map<String, Object> metadata;
}
