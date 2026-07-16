package com.cloudmall.auth.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_auth_user")
public class AuthUserDO {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Long storeId;
    private String userType;    // NORMAL, ADMIN, MERCHANT
    private Integer status;      // 0=disabled, 1=enabled
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}