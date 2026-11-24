package com.cloudmall.mybatisplus.query;

import java.util.function.Function;

import cn.hutool.core.collection.CollStreamUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.common.model.page.PageResp;

/**
 * 分页结果转换器。
 * <p>
 * 把 MP 分页结果转换为可跨服务传输的 {@link PageResp}，
 * 列表元素由调用方提供的单条转换函数完成映射。
 *
 * @author : Tomatos
 */
public final class PageResponses {

    /**
     * 转换分页结果。
     *
     * @param page MP 分页结果
     * @param converter 单条实体到响应的转换函数
     * @param <D> 实体类型
     * @param <R> 响应类型
     * @return 分页响应契约
     */
    public static <D, R> PageResp<R> of(IPage<D> page, Function<D, R> converter) {
        return PageResp.<R>builder()
                       .records(CollStreamUtil.toList(page.getRecords(), converter))
                       .total(page.getTotal())
                       .current(page.getCurrent())
                       .size(page.getSize())
                       .pages(page.getPages())
                       .build();
    }

    private PageResponses() {}
}
