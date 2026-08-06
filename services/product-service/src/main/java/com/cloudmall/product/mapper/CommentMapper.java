package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

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
}
