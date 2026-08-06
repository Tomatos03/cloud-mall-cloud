package com.cloudmall.seckill.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.seckill.api.request.ActivityCreateReq;
import com.cloudmall.seckill.api.request.ActivityProductAuditReq;
import com.cloudmall.seckill.api.request.ActivityProductSubmitReq;
import com.cloudmall.seckill.api.request.ActivityUpdateReq;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀活动与商品编排层
 * <p>
 * 负责上下文解析、远程校验与参数组装：活动创建/编辑/发布/取消取登录态；
 * 商品提交前经 product-service 取 SKU 可信快照，解析 productId / shopId 并校验价格与库存，
 * 避免客户端伪造归属与原价。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class SeckillApp {

    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;
    private final SkuClient skuClient;

    /**
     * 创建活动草稿（从登录态取 createUser）。
     *
     * @param req 创建请求
     * @return 新活动 ID
     */
    public Long createActivity(ActivityCreateReq req) {
        Long createUser = UserContextHolder.getUserId();

        SeckillActivityDO activity = SeckillActivityDO.builder()
                                                      .name(req.getName())
                                                      .description(req.getDescription())
                                                      .startTime(req.getStartTime())
                                                      .maxProducts(req.getMaxProducts())
                                                      .createUser(createUser)
                                                      .build();
        return activityService.createDraft(activity);
    }

    /**
     * 编辑活动。
     *
     * @param id 活动 ID
     * @param req 编辑请求
     */
    public void updateActivity(Long id, ActivityUpdateReq req) {
        SeckillActivityDO update = SeckillActivityDO.builder()
                                                    .name(req.getName())
                                                    .description(req.getDescription())
                                                    .startTime(req.getStartTime())
                                                    .maxProducts(req.getMaxProducts())
                                                    .build();
        activityService.update(id, update);
    }

    /**
     * 发布活动（从登录态取 publishUser）。
     *
     * @param id 活动 ID
     */
    public void publishActivity(Long id) {
        Long publishUser = UserContextHolder.getUserId();
        activityService.publish(id, publishUser);
    }

    /**
     * 取消活动。
     *
     * @param id 活动 ID
     */
    public void cancelActivity(Long id) {
        activityService.cancel(id);
    }

    /**
     * 商家提交秒杀商品。
     *
     * @param activityId 活动 ID
     * @param req 提交请求
     * @return 新秒杀商品 ID
     */
    public Long submitProduct(Long activityId, ActivityProductSubmitReq req) {
        List<SkuResp> skus = skuClient.listSkusByIds(List.of(req.getSkuId()));
        Asserts.notEmpty(skus, BizError.PRODUCT_NOT_FOUND);

        SkuResp sku = skus.get(0);
        Asserts.isTrue(StatusEnum.ENABLED.equals(sku.getStatus()), BizError.PRODUCT_NOT_ACTIVE);

        // SKU 必须属于当前商家店铺，防止越权替他人店铺报名
        Long shopId = UserContextHolder.getShopId();
        Asserts.notNull(shopId, BizError.MERCHANT_STORE_NOT_FOUND);
        Asserts.isEqual(sku.getShopId(), shopId, BizError.MERCHANT_NO_SHOP);

        // 秒杀价必须低于 SKU 原价
        Asserts.isTrue(
                req.getSeckillPrice().compareTo(sku.getPrice()) < 0,
                BizError.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL
        );

        // 秒杀库存不得超过 SKU 库存
        Asserts.isTrue(req.getStock() <= sku.getStock(), BizError.SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK);

        SeckillProductDO product = SeckillProductDO.builder()
                                                   .activityId(activityId)
                                                   .productId(sku.getProductId())
                                                   .skuId(sku.getSkuId())
                                                   .shopId(sku.getShopId())
                                                   .seckillPrice(req.getSeckillPrice())
                                                   .stock(req.getStock())
                                                   .build();
        return productService.submit(product, UserContextHolder.getUserId());
    }

    /**
     * 运营审核秒杀商品（审核人取自登录态）。
     *
     * @param productId 秒杀商品 ID
     * @param req 审核请求
     */
    public void auditProduct(Long productId, ActivityProductAuditReq req) {
        productService.audit(productId, req.isApproved(), req.getAuditRemark(), UserContextHolder.getUserId());
    }
}
