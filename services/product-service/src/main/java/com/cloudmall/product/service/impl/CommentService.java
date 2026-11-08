package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.req.CommentCreateReq;
import com.cloudmall.product.api.model.resp.CommentResp;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.product.convert.CommentConverter;
import com.cloudmall.product.mapper.CommentMapper;
import com.cloudmall.product.model.entity.CommentDO;
import com.cloudmall.product.model.req.CommentPageReq;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.service.ICommentService;
import com.cloudmall.product.service.IProductService;
import com.cloudmall.user.api.client.InternalUserClient;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentMapper commentMapper;
    private final CommentConverter commentConverter;
    private final InternalUserClient internalUserClient;
    private final IProductService productService;

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<CommentResp> page(CommentPageReq req) {
        IPage<CommentDO> page = commentMapper.selectCommentPage(req);
        return page.convert(commentConverter::toResp);
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public void create(CommentCreateReq req) {
        Long userId = Asserts.requireNonNull(
                UserContextHolder.getUserId(),
                BizErrorEnum.USER_NOT_AUTHENTICATED
        );
        ProductResp product = productService.get(ProductGetReq.builder()
                                                              .id(req.getProductId())
                                                              .build());
        Asserts.requireNonNull(product, BizErrorEnum.PRODUCT_NOT_EXIST);
        CommentDO comment = commentConverter.toDO(req, userId);
        comment.setShopId(product.getShopId());
        commentMapper.insert(comment);
        try {
            internalUserClient.completePointTask("DAILY_COMMENT", userId);
        } catch (Exception ignored) {
            // 积分任务属于激励能力，不能阻塞评价主流程。
        }
    }

}
