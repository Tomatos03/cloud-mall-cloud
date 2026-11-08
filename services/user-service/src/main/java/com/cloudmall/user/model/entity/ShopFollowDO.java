package com.cloudmall.user.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户关注店铺关系实体。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_shop_follow")
public class ShopFollowDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 店铺 ID */
    private Long shopId;

    /** 关注时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
