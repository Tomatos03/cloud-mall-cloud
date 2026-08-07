package com.cloudmall.user.service;

import java.util.List;

import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;

public interface IAddressService {

    /**
     * 获取用户所有收货地址
     *
     * @param userId 用户 ID
     * @return 地址列表（上限 10 条，无需分页）
     */
    List<AddressResp> list(Long userId);

    /**
     * 获取用户指定收货地址（不存在或不属于该用户时返回 null）。
     *
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 地址响应
     */
    AddressResp getById(Long id, Long userId);

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @param userId 用户标识
     * @return 结果标识或数值
     */
    Long create(CreateReq request, Long userId);

    /**
     * 更新相关数据。
     *
     * @param request 请求参数
     */
    void update(AddressUpdateReq request);

    /**
     * 删除相关数据。
     *
     * @param ids 对象标识列表
     */
    void delete(List<Long> ids);
}
