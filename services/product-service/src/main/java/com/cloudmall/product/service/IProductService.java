package com.cloudmall.product.service;

import java.util.Collection;
import java.util.List;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.product.api.model.req.ProductCreateReq;
import com.cloudmall.product.api.model.req.ProductUpdateReq;
import com.cloudmall.product.api.model.resp.ProductDetailResp;
import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.product.api.model.resp.ProductResp;
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
     * 游标拉取商品搜索文档。 <p>
     * 供 search-service 全量重建 / 定时增量同步使用， 文档组装逻辑仅此一处（SPU +
     * SKU 最低价 + 分类/品牌名 + 主图）。
     *
     * @param req 游标分页参数（cursor / cursorTime 二选一，见
     *            {@link CursorReq}）
     * @return 游标分页结果
     */
    CursorResult<ProductDocResp> cursorPage(CursorReq req);

    /**
     * 按商品 ID 组装单个搜索文档。 <p>
     * 供 search-service MQ 增量消费（
     * ProductCreated/ProductUpdated）拉取完整文档， 组装逻辑与
     * {@link #cursorPage} 保持一致。
     *
     * @param id SPU ID
     * @return 搜索文档，商品不存在返回 null
     */
    ProductDocResp searchDoc(Long id);

    /**
     * 按分类 ID 集合查询已上架商品（首页分类展示用）。 <p>
     * 分类 ID 集合可包含指定分类及其子分类，文档组装逻辑与 {@link #cursorPage} 保
     * 持一致。
     *
     * @param categoryIds 分类 ID 集合
     * @param limit 查询条数上限（1~50，默认 10）
     * @return 商品搜索文档列表
     */
    List<ProductDocResp> listByCategoryIds(Collection<Long> categoryIds, Integer limit);

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
     * 更新商品（SPU 基本信息 + 规格 + SKU + 图片），并发布 ProductUpdated
     * 事件。 <p>
     * 差异合并语义：按规格组合指纹匹配，已存在 SKU 保留主键/编码/库存， 仅刷新销售字段；新增规格组
     * 合插入新 SKU。
     *
     * @param id SPU ID
     * @param req 更新请求
     */
    void update(Long id, ProductUpdateReq req);

    /**
     * 删除商品（软删除），并发布 ProductDeleted 事件。
     *
     * @param id SPU ID
     */
    void delete(Long id);

    /**
     * 查询 SPU 完整详情（含品牌、图片、SKU、规格）。
     *
     * @param id SPU ID
     * @return 商品完整详情
     */
    ProductDetailResp detail(Long id);
}
