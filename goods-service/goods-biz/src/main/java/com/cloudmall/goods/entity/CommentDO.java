package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cm_comment")
public class CommentDO {
    private Long id;
    private Long goodsId;
    private Long userId;
    private Long orderId;
    private String content;
    private Integer rating;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
}
