package com.cloudmall.search.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.search.config.SearchSyncProperties;
import com.cloudmall.search.index.ProductIndexRepository;

/**
 * 商品索引定时增量同步 JobHandler
 * <p>
 * MQ 增量消费的兜底数据源：按 updateTime 游标周期拉取 product-service
 * docs，批量 upsert 写入 ES，覆盖 MQ 消息丢失/漏消费场景。
 * 游标存 Redis（最近一次成功同步的最大 updateTime，写游标时按
 * {@code cursorBackoffSeconds} 回退，避免边界漏数据）。
 * 删除场景由全量对账任务兜底。由 {@code search.sync.incremental.enabled} 控制是否执行。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIncrementalSyncJobHandler {

    private static final DateTimeFormatter CURSOR_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ProductClient productClient;
    private final ProductIndexRepository indexWriter;
    private final SearchSyncProperties syncProps;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 增量同步商品索引。
     */
    @XxlJob("searchProductIncrementalSync")
    public void incrementalSync() {
        SearchSyncProperties.IncrementalSync incremental = syncProps.getIncremental();
        if (!incremental.isEnabled()) {
            log.info("商品索引增量同步任务已禁用（search.sync.incremental.enabled=false），跳过");
            return;
        }
        LocalDateTime cursorTime = readCursor(incremental.getCursorKey());
        if (cursorTime == null) {
            // 首次运行：游标置为当前时间，仅同步后续变更；存量数据由全量对账任务补齐
            cursorTime = LocalDateTime.now();
            writeCursor(incremental.getCursorKey(), cursorTime);
            log.info("商品索引增量同步：首次运行，游标初始化为 {}", cursorTime);
            return;
        }

        long total = 0;
        long updated = 0;
        LocalDateTime maxUpdateTime = cursorTime;
        long pages = 0;
        while (true) {
            if (incremental.getMaxPages() > 0 && pages >= incremental.getMaxPages()) {
                log.warn("商品索引增量同步达到最大翻页数上限 maxPages={}，提前结束", incremental.getMaxPages());
                break;
            }
            CursorResult<ProductDocResp> result = productClient.cursorPage(
                    CursorReq.builder()
                             .cursorTime(cursorTime)
                             .size(incremental.getPageSize())
                             .build()
            );
            List<ProductDocResp> records = result.getItems();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            total += records.size();
            updated += indexWriter.upsertBatch(records);
            LocalDateTime pageMax = records.stream()
                                           .map(ProductDocResp::getUpdateTime)
                                           .filter(Objects::nonNull)
                                           .max(LocalDateTime::compareTo)
                                           .orElse(null);
            if (pageMax != null && pageMax.isAfter(maxUpdateTime)) {
                maxUpdateTime = pageMax;
            }
            if (!result.getHasMore()) {
                break;
            }
            cursorTime = result.getCursorTime();
            pages++;
        }

        LocalDateTime nextCursor = maxUpdateTime.minusSeconds(incremental.getCursorBackoffSeconds());
        if (nextCursor.isAfter(cursorTime)) {
            writeCursor(incremental.getCursorKey(), nextCursor);
        }
        log.info(
                "商品索引增量同步完成 total={} updated={} failed={} cursor={} -> {}",
                total,
                updated,
                total - updated,
                cursorTime,
                nextCursor
        );
    }

    /**
     * 读取增量游标。
     *
     * @param cursorKey 游标 Redis key
     * @return 游标时间，无记录返回 null
     */
    private LocalDateTime readCursor(String cursorKey) {
        String value = stringRedisTemplate.opsForValue().get(cursorKey);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return LocalDateTime.parse(value, CURSOR_FORMAT);
    }

    /**
     * 写入增量游标。
     *
     * @param cursorKey 游标 Redis key
     * @param cursor 游标时间
     */
    private void writeCursor(String cursorKey, LocalDateTime cursor) {
        stringRedisTemplate.opsForValue().set(cursorKey, cursor.format(CURSOR_FORMAT));
    }
}
