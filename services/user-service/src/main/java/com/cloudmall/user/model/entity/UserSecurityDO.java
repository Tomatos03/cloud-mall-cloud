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
 * 用户账户安全信息。
 * <p>
 * 对应数据库表 cm_user_security，保存支付密码与实名认证信息。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_security")
public class UserSecurityDO {

    @TableId
    private Long userId;

    /** 支付密码 BCrypt 哈希 */
    private String payPasswordHash;

    /** 支付密码设置/修改时间 */
    private LocalDateTime payPasswordSetTime;

    /** 真实姓名密文 */
    private String realName;

    /** 真实姓名脱敏展示值 */
    private String realNameMask;

    /** 身份证号密文 */
    private String idCardNo;

    /** 身份证号脱敏展示值 */
    private String idCardMask;

    /** 实名状态：UNVERIFIED / PENDING / VERIFIED / REJECTED */
    private String realNameStatus;

    private LocalDateTime realNameSubmitTime;

    private LocalDateTime realNameVerifyTime;

    private String realNameRejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
