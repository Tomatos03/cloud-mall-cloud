package com.cloudmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.model.req.ProductPageReq;

/**
 * 商品 SPU 服务接口。
 *
 * @author : Tomatos
 */
public interface IProductService {

    /**
     * 根据条件查询单个 SPU 基本信息。
     *
     * @param req 查询条件（支持按 ID 或 SPU 编码）
     * @return 商品基本信息
     */
    ProductResp get(ProductGetReq req);

    /**
     * 查询 SPU 分页列表（含品牌名、主图、最低价等跨表信息）。
     *
     * @param req 分页 + 筛选条件
     * @return 商品分页列表
     */
    IPage<ProductResp> page(ProductPageReq req);

    /**
     * 创建商品
     *
     * @param req 创建请求
     * @return 新建 SPU ID
     */
    Long create(ProductCreateReq req);

    /**
     * 查询 SPU 完整详情（含品牌、图片、SKU、规格）。
     *
     * @param id SPU ID
     * @return 商品完整详情
     */
    ProductDetailResp detail(Long id);

    /**
     * 更新商品 SKU 库存。
     * <p>
     * quantity 为正数时增加库存，为负数时扣减库存；扣减依赖单条 UPDATE 的行锁与库存条件
     * （stock + quantity >= 0）保证并发安全，不会超卖。
     *
     * @param skuId    SKU ID
     * @param quantity 库存变化量（正数增加，负数扣减）
     * @return 操作是否成功
     */
    Boolean updateStock(Long skuId, Integer quantity);
}
