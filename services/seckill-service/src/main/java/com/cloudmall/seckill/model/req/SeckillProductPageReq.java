package com.cloudmall.seckill.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.seckill.entity.SeckillProductDO;

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
public class SeckillProductPageReq extends PageReq<SeckillProductDO> {

    /** 商品审核状态（为空表示不限） */
    @QueryCondition(property = "auditStatus", operator = Operator.EQ)
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

    /**
     * 构建查询条件：按 ID 倒序。
     *
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<SeckillProductDO> buildLambdaWrapper() {
        LambdaQueryWrapper<SeckillProductDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(SeckillProductDO::getId);
        return wrapper;
    }
}
