package com.cloudmall.seckill.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.cloudmall.common.model.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 秒杀商品分页查询请求。
 * <p>
 * 运营端按审核状态筛选商品，status 为空表示不限。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@AllArgsConstructor
public class SeckillProductPageReq extends PageReq {

    /** 商品审核状态（为空表示不限） */
    @QueryCondition(property = "auditStatus", operator = OperatorEnum.EQ)
    private String status;

    /**
     * 使用默认分页参数构造对象（每页 20 条）。
     */
    public SeckillProductPageReq() {
        super(1, 20);
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public SeckillProductPageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 兼容 URL 参数 ?page=&pageSize=。
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 兼容 URL 参数 ?page=&pageSize=。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        setSize(pageSize);
    }

}
