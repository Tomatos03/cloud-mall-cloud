package com.cloudmall.product.api.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品评论响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResp {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private List<String> images;
    private Integer usefulCount;
    private LocalDateTime createTime;
}
