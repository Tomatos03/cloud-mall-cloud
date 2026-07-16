package com.cloudmall.goods.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_favorite")
public class FavoriteDO {
    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
}