package com.cloudmall.product.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索文档分页响应
 * <p>
 * search-docs 接口的轻量分页契约，避免 Feign 传递
 * MyBatis-Plus {@code IPage} 时对调用方引入 mybatis 依赖。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocPageResp {

    /** 当前页文档列表 */
    private List<SearchDocResp> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;
}
