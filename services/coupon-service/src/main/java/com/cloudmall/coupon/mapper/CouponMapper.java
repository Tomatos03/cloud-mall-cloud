package com.cloudmall.coupon.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.api.enums.CouponOwnerEnum;
import com.cloudmall.coupon.api.enums.CouponStatusEnum;
import com.cloudmall.coupon.api.enums.IssueChannelEnum;
import com.cloudmall.coupon.model.entity.CouponDO;
import com.cloudmall.coupon.model.req.CouponPageReq;

/**
 * 优惠券模板 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 分页查询优惠券模板，按创建时间倒序。 <p>
     * 条件由 {@link CouponPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 模板分页数据
     */
    default IPage<CouponDO> selectCouponPage(CouponPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 查询注册自动发放的可用平台模板。
     *
     * @param now 当前时间
     * @return 模板列表
     */
    default List<CouponDO> selectRegisterAutoTemplates(LocalDateTime now) {
        return selectList(
                Wrappers.<CouponDO>lambdaQuery()
                        .eq(CouponDO::getStatus, CouponStatusEnum.ACTIVE)
                        .eq(CouponDO::getOwnerType, CouponOwnerEnum.PLATFORM)
                        .eq(CouponDO::getIssueChannel, IssueChannelEnum.REGISTER_AUTO)
                        .le(CouponDO::getIssueStartTime, now)
                        .ge(CouponDO::getIssueEndTime, now)
        );
    }

    /**
     * 查询公开券中心全部模板，按 ID 升序。
     *
     * @return 模板列表
     */
    default List<CouponDO> selectPublicClaimTemplates() {
        return selectList(
                Wrappers.<CouponDO>lambdaQuery()
                        .eq(CouponDO::getIssueChannel, IssueChannelEnum.PUBLIC_CLAIM)
                        .orderByAsc(CouponDO::getId)
        );
    }

    /**
     * 按 ID 游标查询公开券中心模板（升序）。
     *
     * @param cursor 上一页最后一条模板 ID（首次传 0）
     * @param size 单页条数
     * @return 模板列表
     */
    default List<CouponDO> selectPublicClaimTemplatesAfter(Long cursor, int size) {
        Page<CouponDO> page = new Page<>(1, size, false);
        return selectPage(page, Wrappers.<CouponDO>lambdaQuery()
                                        .eq(CouponDO::getIssueChannel, IssueChannelEnum.PUBLIC_CLAIM)
                                        .gt(CouponDO::getId, cursor)
                                        .orderByAsc(CouponDO::getId))
                .getRecords();
    }

    /**
     * 原子递增发行数量（并发领取专用）。 <p>
     * 仅当模板处于 ACTIVE、允许公开领取、处于发放窗口内且 有限库存未耗尽时递增，并顺带落库首次发放
     * 时间。
     *
     * @param couponId 模板 ID
     * @return 影响行数，0 表示模板不可领取或库存不足
     */
    default int increaseClaimedCount(Long couponId) {
        LocalDateTime now = LocalDateTime.now();
        return update(
                Wrappers.<CouponDO>lambdaUpdate()
                        .setSql("claimed_count = claimed_count + 1")
                        .setSql("first_issued_time = COALESCE(first_issued_time, NOW())")
                        .set(CouponDO::getUpdateTime, now)
                        .eq(CouponDO::getId, couponId)
                        .eq(CouponDO::getStatus, CouponStatusEnum.ACTIVE)
                        .eq(CouponDO::getIssueChannel, IssueChannelEnum.PUBLIC_CLAIM)
                        .le(CouponDO::getIssueStartTime, now)
                        .ge(CouponDO::getIssueEndTime, now)
                        .and(
                                wrapper -> wrapper.isNull(CouponDO::getTotalCount)
                                                  .or()
                                                  .apply("claimed_count < total_count")
                        )
        );
    }
}
