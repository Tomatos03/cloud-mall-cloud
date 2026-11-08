package com.cloudmall.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.model.resp.PointExchangeItemResp;
import com.cloudmall.user.api.model.resp.PointExchangeResp;
import com.cloudmall.user.api.model.resp.PointTaskResp;
import com.cloudmall.user.api.model.resp.SignInResp;
import com.cloudmall.user.api.model.resp.SignInStatusResp;
import com.cloudmall.user.model.entity.PointExchangeItemDO;
import com.cloudmall.user.model.entity.PointExchangeOrderDO;
import com.cloudmall.user.model.entity.PointTaskDO;
import com.cloudmall.user.model.entity.UserSignInDO;

/**
 * 积分中心转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface PointCenterConverter {

    /**
     * 签到记录 + 最新余额 -> 签到结果。
     *
     * @param signIn 签到记录
     * @param balance 最新余额
     * @return 签到结果
     */
    @Mapping(source = "signIn.streakDays", target = "streakDays")
    @Mapping(source = "signIn.rewardPoints", target = "rewardPoints")
    SignInResp toSignInResp(UserSignInDO signIn, Integer balance);

    /**
     * 签到记录 + 状态 -> 签到状态。
     *
     * @param signIn 签到记录
     * @param streakDays 连续签到天数
     * @param todayReward 今日奖励
     * @param balance 最新余额
     * @return 签到状态
     */
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "streakDays", target = "streakDays")
    @Mapping(source = "todayReward", target = "todayReward")
    @Mapping(target = "signedToday", expression = "java(signIn != null)")
    SignInStatusResp toSignInStatusResp(
        UserSignInDO signIn,
        Integer streakDays,
        Integer todayReward,
        Integer balance
    );

    /**
     * 任务定义 + 完成状态 -> 任务响应。
     *
     * @param task 任务定义
     * @param completed 是否已完成
     * @param claimed 是否已领取
     * @return 任务响应
     */
    @Mapping(source = "claimed", target = "claimed")
    @Mapping(source = "completed", target = "completed")
    PointTaskResp toTaskResp(PointTaskDO task, Boolean completed, Boolean claimed);

    /**
     * 兑换商品 -> 兑换商品响应。
     *
     * @param item 兑换商品
     * @param exchanged 当前用户是否已兑换
     * @return 兑换商品响应
     */
    @Mapping(source = "exchanged", target = "exchanged")
    PointExchangeItemResp toExchangeItemResp(PointExchangeItemDO item, Boolean exchanged);

    /**
     * 兑换记录 + 最新余额 -> 兑换结果。
     *
     * @param order 兑换记录
     * @param balance 最新余额
     * @return 兑换结果
     */
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.pointsCost", target = "pointsCost")
    @Mapping(source = "order.userCouponId", target = "userCouponId")
    PointExchangeResp toExchangeResp(PointExchangeOrderDO order, Integer balance);
}
