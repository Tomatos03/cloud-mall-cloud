package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.entity.BannerDO;
import com.cloudmall.goods.mapper.BannerMapper;
import com.cloudmall.goods.service.IBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements IBannerService {

    private final BannerMapper bannerMapper;

    @Override
    public List<BannerResp> listActive() {
        List<BannerDO> list = bannerMapper.selectList(
                new LambdaQueryWrapper<BannerDO>()
                        .eq(BannerDO::getStatus, 1)
                        .orderByAsc(BannerDO::getSortOrder)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BannerResp toResponse(BannerDO banner) {
        BannerResp r = new BannerResp();
        r.setId(banner.getId());
        r.setTitle(banner.getTitle());
        r.setImage(banner.getImage());
        r.setLinkUrl(banner.getLinkUrl());
        r.setSortOrder(banner.getSortOrder());
        return r;
    }
}
