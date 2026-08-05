package com.cloudmall.coupon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.coupon.api.request.CouponReq;
import com.cloudmall.coupon.api.response.CouponOperationLogResp;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.model.req.CouponOperationLogPageReq;
import com.cloudmall.coupon.model.req.CouponPageReq;

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
     * 店铺 ID 由调用方（
     * {@link com.cloudmall.coupon.application.CouponTemplateApp}
     * ）解析后传入。
     *
     * @param req 创建请求
     * @param shopId 店铺 ID（平台券为 null）
     * @return 新模板 ID
     */
    Long createDraft(CouponReq req, Long shopId);

    /**
     * 编辑模板草稿，仅 DRAFT 状态可编辑。 <p>
     * 店铺 ID 由调用方（
     * {@link com.cloudmall.coupon.application.CouponTemplateApp}
     * ）以已存在模板为准解析后传入。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     * @param shopId 店铺 ID
     */
    void updateDraft(Long id, CouponReq req, Long shopId);

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
     * 商家分页查询自己店铺的模板。
     *
     * @param req 分页请求
     * @return 模板分页
     */
    IPage<CouponResp> pageShop(CouponPageReq req);

    /**
     * 运营分页查询全部模板。
     *
     * @param req 分页请求
     * @return 模板分页
     */
    IPage<CouponResp> pageAll(CouponPageReq req);

    /**
     * 运营分页查询模板操作流水。
     *
     * @param req 分页请求
     * @return 操作流水分页
     */
    IPage<CouponOperationLogResp> pageOperationLogs(CouponOperationLogPageReq req);

    /**
     * 查询模板实体，不存在时抛出异常。
     *
     * @param id 模板 ID
     * @return 模板实体
     */
    CouponDO getTemplate(Long id);
}
