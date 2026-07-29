package com.cloudmall.auth.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.model.req.UserPageReq;


/**
 * 用户服务接口
 * <p>
 * 提供认证（登录/注册）、用户信息查询以及用户-角色关联查询。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
public interface IUserService {

    LoginResp login(LoginReq request);

    Long register(RegisterReq request);

    UserInfoResp getUserInfo(Long userId);

    /**
     * 分页查询用户信息
     *
     * @param req 分页请求（含筛选条件）
     * @return 分页结果
     */
    IPage<UserInfoResp> pageUserInfos(UserPageReq req);

    /**
     * 获取用户已分配的角色 ID 列表
     *
     * @param userId 用户 ID
     * @return 角色 ID 列表
     */
    List<Long> getUserRoleIds(Long userId);
}
