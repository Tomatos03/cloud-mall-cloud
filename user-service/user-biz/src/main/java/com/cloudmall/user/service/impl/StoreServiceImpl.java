package com.cloudmall.user.service.impl;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.user.api.response.StoreResponse;
import com.cloudmall.user.entity.StoreDO;
import com.cloudmall.user.mapper.StoreMapper;
import com.cloudmall.user.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements IStoreService {

    private final StoreMapper storeMapper;

    @Override
    public StoreResponse getByUserId(Long userId) {
        StoreDO store = storeMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StoreDO>()
                .eq(StoreDO::getUserId, userId)
        );
        if (store == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        return toResponse(store);
    }

    @Override
    public StoreResponse getById(Long id) {
        StoreDO store = storeMapper.selectById(id);
        if (store == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        return toResponse(store);
    }

    private StoreResponse toResponse(StoreDO store) {
        StoreResponse r = new StoreResponse();
        r.setId(store.getId());
        r.setUserId(store.getUserId());
        r.setStoreName(store.getStoreName());
        r.setStoreLogo(store.getStoreLogo());
        r.setDescription(store.getDescription());
        r.setStatus(store.getStatus());
        return r;
    }
}
