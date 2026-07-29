package com.cloudmall.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.convert.StoreConverter;
import com.cloudmall.user.entity.StoreDO;
import com.cloudmall.user.mapper.StoreMapper;
import com.cloudmall.user.model.req.StoreGetReq;
import com.cloudmall.user.service.IStoreService;

@Service
@RequiredArgsConstructor
public class StoreService implements IStoreService {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    @Override
    public StoreResp get(StoreGetReq req) {
        StoreDO store = storeMapper.selectOne(req.buildLambdaWrapper());
        return storeConverter.toResp(store);
    }
}
