package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.entity.CommentDO;
import com.cloudmall.product.model.req.CommentPageReq;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {

    /**
     * 分页查询评论（条件由 {@link CommentPageReq} 的
     *
     * @QueryCondition 注解声明）。
     * @param req 分页 + 筛选条件
     * @return 评论分页数据
     */
    default IPage<CommentDO> selectCommentPage(CommentPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 查询商品全部可见评论（状态启用，软删除由 @TableLogic 自动过滤）。
     *
     * @param productId 商品 ID
     * @return 评论列表
     */
    default List<CommentDO> selectVisibleByProduct(Long productId) {
        return selectList(
                Wrappers.<CommentDO>lambdaQuery()
                        .eq(CommentDO::getProductId, productId)
                        .eq(CommentDO::getStatus, StatusEnum.ENABLED)
        );
    }
}
