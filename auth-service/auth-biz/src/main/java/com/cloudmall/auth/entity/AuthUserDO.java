package com.cloudmall.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
    private String userType;    // NORMAL, ADMIN, MERCHANT
    private Integer status;      // 0=disabled, 1=enabled
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
