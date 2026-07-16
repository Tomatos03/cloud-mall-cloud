package com.cloudmall.goods.api.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResp {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createTime;
}
