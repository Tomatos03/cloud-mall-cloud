package com.cloudmall.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.convert.StoreConverter;
import com.cloudmall.user.entity.StoreDO;
import com.cloudmall.user.mapper.StoreMapper;
import com.cloudmall.user.service.IStoreService;

@Service
@RequiredArgsConstructor
public class StoreService implements IStoreService {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    @Override
    public StoreResp getByUserId(Long userId) {
        StoreDO store = storeMapper.selectOne(
            Wrappers.<StoreDO>lambdaQuery()
                .eq(StoreDO::getUserId, userId)
        );
        AssertUtils.notNull(store, BizErrorCode.DATA_NOT_FOUND);
        return storeConverter.toResp(store);
    }

    @Override
    public StoreResp getById(Long id) {
        StoreDO store = storeMapper.selectById(id);
        AssertUtils.notNull(store, BizErrorCode.DATA_NOT_FOUND);
        return storeConverter.toResp(store);
    }
}
