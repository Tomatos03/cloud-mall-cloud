package com.cloudmall.goods.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentCreateReq {

    private Long goodsId;
    private Long userId;
    private String content;
    private Integer rating;
}