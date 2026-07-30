package com.cloudmall.product.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentCreateReq {
    private Long productId;
    private Long userId;
    private String content;
    private Integer rating;
}