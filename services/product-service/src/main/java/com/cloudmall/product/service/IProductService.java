package com.cloudmall.product.service;

import java.time.LocalDateTime;

import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.api.response.SearchDocPageResp;
import com.cloudmall.product.model.req.ProductGetReq;

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
     * 分页拉取商品搜索文档。 <p>
     * 供 search-service 全量重建 / 定时增量同步使用， 文档组装逻辑仅此一处（SPU +
     * SKU 最低价 + 分类/品牌名 + 主图）。
     *
     * @param current 页码，从 1 开始
     * @param size 每页条数
     * @param since 增量游标（可选），仅返回 updateTime 晚于 since 的商品
     * @return 搜索文档分页结果
     */
    SearchDocPageResp searchDocs(long current, long size, LocalDateTime since);

    /**
     * 创建商品。 <p>
     * 店铺 ID 由调用方（
     * {@link com.cloudmall.product.application.ProductApp}
     * ） 经远程调用解析后传入。
     *
     * @param req 创建请求
     * @param shopId 店铺 ID
     * @return 新建 SPU ID
     */
    Long create(ProductCreateReq req, Long shopId);

    /**
     * 查询 SPU 完整详情（含品牌、图片、SKU、规格）。
     *
     * @param id SPU ID
     * @return 商品完整详情
     */
    ProductDetailResp detail(Long id);
}
