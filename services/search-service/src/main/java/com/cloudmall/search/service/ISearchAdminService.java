package com.cloudmall.search.service;

import com.cloudmall.search.model.RebuildResultResp;

/**
 * 搜索管理服务接口
 *
 * @author : Tomatos
 */
public interface ISearchAdminService {

    /**
     * 全量重建商品索引：翻页拉取 product-service 全量搜索文档写入 ES， 并清理 ES
     * 中已不存在的残留文档。 <p>
     * 使用字段级更新（doc 参数不含 sales），不会覆盖销量。
     *
     * @return 重建统计结果
     */
    RebuildResultResp rebuild();
}
