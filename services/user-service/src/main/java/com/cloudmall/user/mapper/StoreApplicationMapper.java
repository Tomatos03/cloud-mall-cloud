package com.cloudmall.user.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.api.enums.StoreApplicationStatusEnum;
import com.cloudmall.user.model.entity.StoreApplicationDO;

/**
 * 商家入驻申请 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface StoreApplicationMapper extends BaseMapper<StoreApplicationDO> {

    /**
     * 按用户查询入驻申请。
     *
     * @param uid 用户 ID
     * @return 入驻申请，不存在时返回 null
     */
    default StoreApplicationDO selectByUserId(Long uid) {
        return selectOne(Wrappers.<StoreApplicationDO>lambdaQuery().eq(StoreApplicationDO::getUserId, uid));
    }

    /**
     * 查询提交时间早于阈值的待审核申请。
     *
     * @param threshold 提交时间阈值
     * @param limit 单批数量
     * @return 待审核申请
     */
    default List<StoreApplicationDO> selectPendingBefore(LocalDateTime threshold, int limit) {
        Page<StoreApplicationDO> page = new Page<>(1, limit, false);
        return selectPage(page, Wrappers.<StoreApplicationDO>lambdaQuery()
                                        .eq(StoreApplicationDO::getStatus, StoreApplicationStatusEnum.PENDING)
                                        .le(StoreApplicationDO::getSubmitTime, threshold)
                                        .orderByAsc(StoreApplicationDO::getId))
                .getRecords();
    }

    /**
     * 清除上一次审核信息（驳回后重新提交时使用）。
     *
     * @param id 申请 ID
     * @return 影响行数
     */
    default int clearAuditInfo(Long id) {
        return update(
                Wrappers.<StoreApplicationDO>lambdaUpdate()
                        .set(StoreApplicationDO::getAuditRemark, null)
                        .set(StoreApplicationDO::getAuditorId, null)
                        .set(StoreApplicationDO::getAuditTime, null)
                        .set(StoreApplicationDO::getShopId, null)
                        .eq(StoreApplicationDO::getId, id)
        );
    }

    /**
     * 审核申请（仅 PENDING 可审核，条件更新防并发）。
     *
     * @param id 申请 ID
     * @param status 目标状态
     * @param remark 审核意见
     * @param auditorId 审核人 ID
     * @param auditTime 审核时间
     * @param shopId 开通店铺 ID
     * @return 影响行数
     */
    default int markReviewed(
        Long id,
        StoreApplicationStatusEnum status,
        String remark,
        Long auditorId,
        LocalDateTime auditTime,
        Long shopId
    ) {
        return update(
                Wrappers.<StoreApplicationDO>lambdaUpdate()
                        .set(StoreApplicationDO::getStatus, status)
                        .set(StoreApplicationDO::getAuditRemark, remark)
                        .set(StoreApplicationDO::getAuditorId, auditorId)
                        .set(StoreApplicationDO::getAuditTime, auditTime)
                        .set(StoreApplicationDO::getShopId, shopId)
                        .eq(StoreApplicationDO::getId, id)
                        .eq(StoreApplicationDO::getStatus, StoreApplicationStatusEnum.PENDING)
        );
    }
}
