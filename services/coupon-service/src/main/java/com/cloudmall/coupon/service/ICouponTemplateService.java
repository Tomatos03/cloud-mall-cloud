package com.cloudmall.coupon.service;

import com.cloudmall.coupon.api.request.CouponBaseReq;
import com.cloudmall.coupon.api.request.CouponUpdateReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;

/**
 * 优惠券模板领域服务
 * <p>
 * 负责模板创建、草稿编辑、发布、暂停、恢复、结束、软删除，
 * 以及核心规则冻结、归属与范围校验和有效期计算。
 *
 * @author : Tomatos
 */
public interface ICouponTemplateService {

    /**
     * 创建模板草稿。 <p>
     * ownerType=SHOP 时 shopId 取自登录令牌；平台券 shopId 恒为空。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    Long createDraft(CouponBaseReq req);

    /**
     * 编辑模板草稿，仅 DRAFT 状态可编辑。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     */
    void updateDraft(Long id, CouponUpdateReq req);

    /**
     * 发布模板草稿：DRAFT → ACTIVE。
     *
     * @param id 模板 ID
     */
    void publish(Long id);

    /**
     * 暂停发放：ACTIVE → PAUSED。
     *
     * @param id 模板 ID
     */
    void pause(Long id);

    /**
     * 恢复发放：PAUSED → ACTIVE。
     *
     * @param id 模板 ID
     */
    void resume(Long id);

    /**
     * 结束发放：ACTIVE/PAUSED → ENDED，不可恢复。
     *
     * @param id 模板 ID
     */
    void end(Long id);

    /**
     * 删除未发放草稿（软删除），仅 DRAFT 且从未发放可删除。
     *
     * @param id 模板 ID
     */
    void deleteDraft(Long id);

    /**
     * 查询模板详情（含商品/分类范围）。
     *
     * @param id 模板 ID
     * @return 模板响应
     */
    CouponResp getDetail(Long id);

    /**
     * 查询模板实体，不存在时抛出异常。
     *
     * @param id 模板 ID
     * @return 模板实体
     */
    CouponDO getTemplate(Long id);
}
