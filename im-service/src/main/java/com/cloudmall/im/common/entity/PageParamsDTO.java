package com.cloudmall.im.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共分页查询基类
 *
 * @author : Tomatos
 * @date : 2026/1/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParamsDTO {
    /**
     * 当前页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
