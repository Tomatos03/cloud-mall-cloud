package com.cloudmall.product.api.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建商品评论请求。
 * <p>
 * userId 由服务端从当前登录用户上下文获取，客户端无需也不应传入。
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentCreateReq {

    /** 商品 ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 评分（1-5） */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer rating;

    /** 评论图片 URL 列表，可选，最多 5 张 */
    @Size(max = 5, message = "评论图片最多5张")
    private List<String> images;
}
