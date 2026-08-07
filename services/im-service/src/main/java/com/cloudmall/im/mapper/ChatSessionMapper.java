package com.cloudmall.im.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.im.entity.ChatSessionDO;
import com.cloudmall.im.model.req.SessionPageReq;

/**
 * 聊天会话 Mapper
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {

    /**
     * 根据买家与商家用户查询会话。
     *
     * @param buyerId 买家用户ID
     * @param merchantId 商家用户ID
     * @return 会话实体，不存在时返回 null
     */
    default ChatSessionDO selectByBuyerAndMerchant(Long buyerId, Long merchantId) {
        return selectOne(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getBuyerId, buyerId)
                        .eq(ChatSessionDO::getMerchantId, merchantId)
        );
    }

    /**
     * 分页查询当前用户参与的会话，按创建时间倒序。 <p>
     * 常见条件由 {@link SessionPageReq} 的
     * {@code @QueryCondition} 注解声明， 成员 OR 条件为特殊查询，在此追加。
     *
     * @param currentUserId 当前用户ID
     * @param req 分页请求
     * @return 会话分页数据
     */
    default IPage<ChatSessionDO> pageByMember(Long currentUserId, SessionPageReq req) {
        LambdaQueryWrapper<ChatSessionDO> wrapper = req.buildLambdaWrapper();
        wrapper.and(
                w -> w.eq(ChatSessionDO::getBuyerId, currentUserId)
                      .or()
                      .eq(ChatSessionDO::getMerchantId, currentUserId)
        );
        return selectPage(req, wrapper);
    }
}
