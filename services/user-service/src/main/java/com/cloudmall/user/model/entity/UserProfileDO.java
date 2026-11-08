package com.cloudmall.user.model.entity;

import java.time.LocalDate;
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
 * 用户扩展资料与设置。
 * <p>
 * 对应数据库表 cm_user_profile，按 user_id 与 cm_user 一对一。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_profile")
public class UserProfileDO {

    @TableId
    private Long userId;

    /** 性别：男 / 女 / 保密 */
    private String gender;

    /** 生日 */
    private LocalDate birthday;

    /** 个人主页可见范围：PUBLIC / PRIVATE */
    private String profileVisibility;

    /** 购买记录可见范围：PUBLIC / PRIVATE */
    private String purchaseVisibility;

    /** 订单通知开关 */
    private Boolean notifyOrder;

    /** 促销通知开关 */
    private Boolean notifyPromotion;

    /** 物流通知开关 */
    private Boolean notifyLogistics;

    /** 社区通知开关 */
    private Boolean notifyCommunity;

    /** 个性化推荐开关 */
    private Boolean recommendEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
