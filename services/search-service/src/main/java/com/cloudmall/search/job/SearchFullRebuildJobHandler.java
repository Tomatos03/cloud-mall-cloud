package com.cloudmall.search.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.search.config.SearchSyncProperties;
import com.cloudmall.search.model.RebuildResultResp;
import com.cloudmall.search.service.ISearchService;

/**
 * 商品索引全量对账 JobHandler
 * <p>
 * 兜底/初始化数据源：翻页拉取 product-service docs 全量文档，
 * 写入 ES 并清理 DB 已不存在的残留文档（删除兜底）。
 * 由 {@code search.sync.full.enabled} 控制是否执行。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFullRebuildJobHandler {

    private final ISearchService searchService;
    private final SearchSyncProperties syncProps;

    /**
     * 全量对账商品索引。
     */
    @XxlJob("searchProductFullRebuild")
    public void fullRebuild() {
        if (!syncProps.getFull().isEnabled()) {
            log.info("商品索引全量对账任务已禁用（search.sync.full.enabled=false），跳过");
            return;
        }
        RebuildResultResp result = searchService.rebuild();
        log.info(
                "商品索引全量对账完成 total={} updated={} deleted={} failed={}",
                result.getTotal(),
                result.getUpdated(),
                result.getDeleted(),
                result.getFailed()
        );
    }
}
