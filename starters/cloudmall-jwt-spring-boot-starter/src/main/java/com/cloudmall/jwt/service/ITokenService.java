package com.cloudmall.jwt.service;

import java.util.Map;

import com.cloudmall.jwt.exception.ParseTokenException;
import com.cloudmall.jwt.model.ParsedToken;

/**
 * 认证令牌服务接口
 * <p>
 * 定义 token 的创建、解析、校验三大核心能力。
 * 具体实现可自由切换：JWT、OAuth2、Session 等。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public interface ITokenService {

    /**
     * 创建令牌 <p>
     * 登录成功后，将声明集合编码为 token 字符串返回给客户端。
     *
     * @param claims 声明集合（含 userId、username、userType 等）
     * @return token 字符串
     */
    String createToken(Map<String, Object> claims);

    /**
     * 解析令牌 <p>
     * 校验签名与有效期后返回只读的 {@link ParsedToken}。解析失败应抛出异常，而非返回 null。
     *
     * @param token token 字符串
     * @return 解析后的 token
     * @throws ParseTokenException token 无效或已过期时抛出
     */
    ParsedToken parseToken(String token);

    /**
     * 校验令牌是否合法 <p>
     * 校验签名与有效期，解析失败时返回 false。
     *
     * @param token token 字符串
     * @return true 合法，false 无效或已过期
     */
    default boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ParseTokenException e) {
            return false;
        }
    }
}
