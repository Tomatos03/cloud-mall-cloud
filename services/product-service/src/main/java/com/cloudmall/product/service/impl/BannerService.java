package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.response.BannerResp;
import com.cloudmall.product.convert.BannerConverter;
import com.cloudmall.product.entity.BannerDO;
import com.cloudmall.product.mapper.BannerMapper;
import com.cloudmall.product.model.req.BannerPageReq;
import com.cloudmall.product.model.req.BannerReq;
import com.cloudmall.product.service.IBannerService;

@Service
@RequiredArgsConstructor
public class BannerService implements IBannerService {

    private final BannerMapper bannerMapper;
    private final BannerConverter bannerConverter;

    @Override
    public List<BannerResp> list(BannerReq req) {
        List<BannerDO> list = bannerMapper.selectList(req.buildLambdaWrapper());
        return bannerConverter.toRespList(list);
    }

    @Override
    public IPage<BannerResp> page(BannerPageReq req) {
        return bannerMapper.selectPage(req, req.buildLambdaWrapper())
                           .convert(bannerConverter::toResp);
    }
}
