package com.cloudmall.product.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 查询数据列表。
     *
     * @param req 请求参数
     * @return 数据列表
     */
    @Override
    public List<BannerResp> list(BannerReq req) {
        List<BannerDO> list = bannerMapper.selectList(req.buildLambdaWrapper());
        return bannerConverter.toRespList(list);
    }

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<BannerResp> page(BannerPageReq req) {
        IPage<BannerDO> page = bannerMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(bannerConverter::toResp);
    }
}
