package com.cloudmall.goods.api.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentResp {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createTime;
}