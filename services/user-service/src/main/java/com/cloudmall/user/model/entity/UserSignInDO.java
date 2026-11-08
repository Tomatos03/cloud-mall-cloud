package com.cloudmall.user.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户签到记录。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_sign_in")
public class UserSignInDO {

    @TableId
    private Long id;

    private Long userId;

    private LocalDate signDate;

    private Integer streakDays;

    private Integer rewardPoints;

    private LocalDateTime createTime;
}
