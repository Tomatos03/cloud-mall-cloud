package com.cloudmall.goods.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.entity.BannerDO;
import com.cloudmall.goods.convert.BannerConverter;
import com.cloudmall.goods.mapper.BannerMapper;
import com.cloudmall.goods.service.IBannerService;

@Service
@RequiredArgsConstructor
public class BannerService implements IBannerService {

    private final BannerMapper bannerMapper;
    private final BannerConverter bannerConverter;

    @Override
    public List<BannerResp> listActive() {
        List<BannerDO> list = bannerMapper.selectList(
                Wrappers.<BannerDO>lambdaQuery()
                        .eq(BannerDO::getStatus, 1)
                        .orderByAsc(BannerDO::getSortOrder)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BannerResp toResponse(BannerDO banner) {
        return bannerConverter.toResp(banner);
    }
}
