package com.cloudmall.goods.api.request;

import lombok.Data;

@Data
public class CommentCreateReq {

    private Long goodsId;
    private Long userId;
    private String content;
    private Integer rating;
}
