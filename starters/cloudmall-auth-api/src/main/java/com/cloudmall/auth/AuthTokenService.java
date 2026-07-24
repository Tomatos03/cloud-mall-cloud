package com.cloudmall.auth;

import com.cloudmall.context.UserContext;

/**
 * 认证令牌服务接口
 * <p>
 * 定义 token 的创建、解析、校验三大核心能力。
 * 具体实现可自由切换：JWT、OAuth2、Session 等。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public interface AuthTokenService {

    /**
     * 创建令牌
     * <p>
     * 登录成功后，将用户上下文编码为 token 字符串返回给客户端。
     *
     * @param userContext 用户上下文（含 userId、username、userType 等）
     * @return token 字符串
     */
    String createToken(UserContext userContext);

    /**
     * 解析令牌
     * <p>
     * 从 token 字符串中还原用户上下文。解析失败应抛出异常，而非返回 null。
     *
     * @param token token 字符串
     * @return 用户上下文
     * @throws com.cloudmall.auth.AuthTokenException token 无效或已过期时抛出
     */
    UserContext parseToken(String token);

    /**
     * 校验令牌是否合法
     * <p>
     * 仅验证签名和有效期，不还原用户上下文。
     *
     * @param token token 字符串
     * @return true 合法，false 无效或已过期
     */
    boolean validateToken(String token);
}
