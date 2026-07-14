package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cm_favorite")
public class FavoriteDO {
    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
}
