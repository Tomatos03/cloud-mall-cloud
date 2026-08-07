package com.cloudmall.seckill.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.seckill.enums.SeckillActivityStatusEnum;

/**
 * 秒杀活动实体
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_seckill_activity")
public class SeckillActivityDO {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 每活动最大商品数 */
    private Integer maxProducts;

    private String status;

    private Long createUserId;

    private Long publishUserId;

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;

    /**
     * 是否处于草稿状态。
     *
     * @return true 表示草稿
     */
    public boolean isDraft() {
        return SeckillActivityStatusEnum.DRAFT.getCode().equals(this.status);
    }

    /**
     * 是否处于报名中状态（已发布）。
     *
     * @return true 表示报名中
     */
    public boolean isPublished() {
        return SeckillActivityStatusEnum.PUBLISHED.getCode().equals(this.status);
    }

    /**
     * 活动是否尚未开始。
     *
     * @param now 当前时间
     * @return true 表示未开始
     */
    public boolean isNotStarted(LocalDateTime now) {
        return this.startTime.isAfter(now);
    }

    /**
     * 是否处于进行中状态。
     *
     * @return true 表示进行中
     */
    public boolean isRunning() {
        return SeckillActivityStatusEnum.RUNNING.getCode().equals(this.status);
    }

    /**
     * 活动是否已开始（到达开始时间）。
     *
     * @param now 当前时间
     * @return true 表示已开始
     */
    public boolean isStarted(LocalDateTime now) {
        return !this.startTime.isAfter(now);
    }

    /**
     * 活动是否尚未结束。
     *
     * @param now 当前时间
     * @return true 表示未结束
     */
    public boolean isNotEnded(LocalDateTime now) {
        return this.endTime.isAfter(now);
    }
}
