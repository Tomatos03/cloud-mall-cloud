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
 * 用户积分任务记录。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_point_task_record")
public class PointTaskRecordDO {

    @TableId
    private Long id;

    private Long userId;

    private String taskCode;

    private LocalDate taskDate;

    /** COMPLETED / CLAIMED */
    private String status;

    private Integer rewardPoints;

    private LocalDateTime createTime;

    private LocalDateTime claimTime;
}
