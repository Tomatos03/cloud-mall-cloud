package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.convert.BannerConverter;
import com.cloudmall.goods.entity.BannerDO;
import com.cloudmall.goods.mapper.BannerMapper;
import com.cloudmall.goods.model.req.BannerPageReq;
import com.cloudmall.goods.model.req.BannerReq;
import com.cloudmall.goods.service.IBannerService;

@Service
@RequiredArgsConstructor
public class BannerService implements IBannerService {

    private final BannerMapper bannerMapper;
    private final BannerConverter bannerConverter;

    @Override
    public List<BannerResp> list(BannerReq req) {
        List<BannerDO> list = bannerMapper.selectList(req.buildLambdaWrapper());
        return list.stream()
                   .map(bannerConverter::toResp)
                   .toList();
    }

    @Override
    public IPage<BannerResp> page(BannerPageReq req) {
        return bannerMapper.selectPage(req, req.buildLambdaWrapper())
                           .convert(bannerConverter::toResp);
    }
}
