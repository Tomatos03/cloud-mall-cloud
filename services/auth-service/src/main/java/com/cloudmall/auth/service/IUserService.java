package com.cloudmall.auth.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.entity.UserDO;
import com.cloudmall.auth.model.req.UserPageReq;
import com.cloudmall.auth.model.resp.ResourceNodeResp;

/**
 * 用户服务接口
 * <p>
 * 提供认证（登录/注册）、用户信息查询以及用户-角色关联查询。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
public interface IUserService {

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    UserDO findByUsername(String username);

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @return 结果标识或数值
     */
    Long register(RegisterReq request);

    /**
     * 获取相关数据。
     *
     * @param userId 用户标识
     * @return 处理结果
     */
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

    /**
     * 获取当前用户可访问的资源树（按 parentId 构建层级） <p>
     * 仅返回类型为 menu / catalog / layout 的 UI 资源，排除按钮类型。 已按当
     * 前用户的权限编码过滤，仅包含用户有权限访问的节点。
     *
     * @return 资源树的根节点列表（children 引用子节点）
     */
    List<ResourceNodeResp> getMenuResourceTree();
}
