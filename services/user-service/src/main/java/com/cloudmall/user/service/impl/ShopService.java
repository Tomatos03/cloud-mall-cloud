package com.cloudmall.user.service.impl;

import java.util.Collections;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.convert.ShopConverter;
import com.cloudmall.user.entity.ShopDO;
import com.cloudmall.user.mapper.ShopMapper;
import com.cloudmall.user.model.req.ShopGetReq;
import com.cloudmall.user.service.IShopService;

@Service
@RequiredArgsConstructor
public class ShopService implements IShopService {

    private final ShopMapper shopMapper;
    private final ShopConverter shopConverter;

    /**
     * 获取相关数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Override
    public ShopResp get(ShopGetReq req) {
        ShopDO shop = shopMapper.selectOne(req.buildLambdaWrapper());
        return shopConverter.toResp(shop);
    }

    /**
     * 查询数据列表。
     *
     * @param shopIds 标识列表
     * @return 数据列表
     */
    @Override
    public List<ShopResp> listByIds(List<Long> shopIds) {
        if (CollUtil.isEmpty(shopIds)) {
            return Collections.emptyList();
        }
        List<ShopDO> shops = shopMapper.selectByIds(shopIds);
        return shopConverter.toRespList(shops);
    }
}
