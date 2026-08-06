package com.cloudmall.seckill.enums;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 秒杀活动状态迁移表
 * <p>
 * 定义活动状态的合法迁移集合，与 {@link SeckillActivityStatusEnum} 配合使用。
 * DRAFT/PUBLISHED 可取消；RUNNING 只能自然结束；FINISHED、CANCELLED 为终态。
 *
 * @author : Tomatos
 */
public final class SeckillActivityStateTransitions {

    private static final Map<SeckillActivityStatusEnum, Set<SeckillActivityStatusEnum>> TRANSITIONS = Map.of(
            SeckillActivityStatusEnum.DRAFT,
            Set.of(SeckillActivityStatusEnum.PUBLISHED, SeckillActivityStatusEnum.CANCELLED),
            SeckillActivityStatusEnum.PUBLISHED,
            Set.of(SeckillActivityStatusEnum.RUNNING, SeckillActivityStatusEnum.CANCELLED),
            SeckillActivityStatusEnum.RUNNING,
            Set.of(SeckillActivityStatusEnum.FINISHED),
            SeckillActivityStatusEnum.FINISHED,
            Collections.emptySet(),
            SeckillActivityStatusEnum.CANCELLED,
            Collections.emptySet()
    );

    private SeckillActivityStateTransitions() {}

    /**
     * 判断活动状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    public static boolean canTransition(SeckillActivityStatusEnum from, SeckillActivityStatusEnum to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }
}
