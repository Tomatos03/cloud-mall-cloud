package com.cloudmall.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 搜索同步配置属性
 * <p>
 * 对应 Nacos 配置 search-service.yaml 的 {@code search.sync.*} 段。
 * 定时同步作为 MQ 增量消费的兜底数据源：增量任务按游标周期拉取变更文档，
 * 全量任务分页拉取全部文档并对账（含删除兜底），保证最终一致。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "search.sync")
public class SearchSyncProperties {

    /** 全量对账任务配置 */
    private FullSync full = new FullSync();

    /** 增量同步任务配置 */
    private IncrementalSync incremental = new IncrementalSync();

    /**
     * 全量对账任务配置。
     */
    @Data
    public static class FullSync {

        /** 是否启用全量对账任务 */
        private boolean enabled = true;

        /** 全量对账每页条数 */
        private int pageSize = 100;

        /** 全量对账最大翻页数，0 表示不限制 */
        private int maxPages = 0;
    }

    /**
     * 增量同步任务配置。
     */
    @Data
    public static class IncrementalSync {

        /** 是否启用增量同步任务 */
        private boolean enabled = true;

        /** 增量同步每页条数 */
        private int pageSize = 100;

        /** 增量游标 Redis key（存最近一次成功同步的最大 updateTime） */
        private String cursorKey = "search:product:sync-cursor";

        /**
         * 游标回退秒数。
         * <p>
         * 写游标时向前回退该秒数，避免同一 updateTime 有多行且分布在
         * 查询快照前后时，下一轮以严格大于游标过滤而漏掉边界行；
         * 重复拉取由幂等 upsert 兜住。
         */
        private int cursorBackoffSeconds = 1;

        /** 增量同步最大翻页数，0 表示不限制 */
        private int maxPages = 0;
    }
}
