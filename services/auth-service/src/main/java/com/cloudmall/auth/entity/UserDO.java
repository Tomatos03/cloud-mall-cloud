package com.cloudmall.auth.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.auth.enums.UserTypeEnum;

/**
 * 用户实体
 * <p>
 * 对应数据库表 cm_user，记录用户认证与基本信息。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user")
public class UserDO {

    @TableId
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    /** 个人简介 */
    private String bio;

    /** 头像 URL */
    private String avatarUrl;

    /** 用户类型（普通用户 / 管理员 / 店铺商家） */
    private UserTypeEnum types;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
