package com.cloudmall.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全量重建结果
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebuildResultResp {

    /** 从 product-service 拉取的商品文档总数 */
    private Long total;

    /** 成功写入/更新的文档数 */
    private Long updated;

    /** 清理的 ES 残留文档数（DB 中已不存在的商品） */
    private Long deleted;
}
