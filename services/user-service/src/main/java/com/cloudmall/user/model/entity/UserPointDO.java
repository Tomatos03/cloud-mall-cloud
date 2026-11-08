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
 * 用户积分账户。
 * <p>
 * 对应数据库表 cm_user_point，按 user_id 与 cm_user 一对一。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_point")
public class UserPointDO {

    @TableId
    private Long userId;

    /** 当前可用积分 */
    private Integer balance;

    /** 累计获得积分 */
    private Integer totalEarned;

    /** 累计消耗积分 */
    private Integer totalSpent;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
