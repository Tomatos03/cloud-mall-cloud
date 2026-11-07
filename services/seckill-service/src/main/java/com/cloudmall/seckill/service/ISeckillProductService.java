package com.cloudmall.seckill.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.model.entity.SeckillProductDO;
import com.cloudmall.seckill.model.req.SeckillProductPageReq;

/**
 * 秒杀商品与审核服务
 *
 * @author : Tomatos
 */
public interface ISeckillProductService {

    /**
     * 商家提交秒杀商品（按 SKU 报名）。
     *
     * @param product 秒杀商品实体
     * @param submitterId 提交人用户 ID
     * @return 新商品 ID
     */
    Long submit(SeckillProductDO product, Long submitterId);

    /**
     * 运营审核秒杀商品。
     *
     * @param productId 秒杀商品 ID
     * @param approved 是否通过
     * @param auditRemark 审核意见
     * @param auditorId 审核人 ID
     */
    void audit(Long productId, boolean approved, String auditRemark, Long auditorId);

    /**
     * 分页查询秒杀商品（运营端按审核状态筛选）。
     *
     * @param req 分页请求
     * @return 商品分页
     */
    IPage<SeckillProductResp> pageProducts(SeckillProductPageReq req);

    /**
     * 查询活动中某 SKU 已审核通过的商品（抢购编排用）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    SeckillProductDO getApprovedByActivityAndSku(Long activityId, Long skuId);

    /**
     * 查询店铺提交的全部商品（商家端“我的提交”）。
     *
     * @param shopId 店铺 ID
     * @return 商品列表
     */
    List<SeckillProductResp> listByShopId(Long shopId);

    /**
     * 校验活动中某商品是否已审核通过（内部校验用）。
     *
     * @param activityId 活动 ID
     * @param productId 商品 ID
     * @return 已审核通过返回 true
     */
    boolean isApproved(Long activityId, Long productId);
}
