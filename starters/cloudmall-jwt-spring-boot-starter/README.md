# cloudmall-jwt-spring-boot-starter

JWT 认证令牌 starter，基于 JJWT（HMAC-SHA 签名），提供令牌创建、解析、校验三大能力，并抽象为 `ITokenService` 接口便于替换实现。

## 功能

### 1. `ITokenService` 接口

```java
public interface ITokenService {

    /** 创建令牌：登录成功后，将用户上下文编码为 token */
    String createToken(UserContext userContext);

    /** 解析令牌：还原用户上下文，无效或过期抛 AuthTokenException */
    UserContext parseToken(String token);

    /** 校验令牌：仅验证签名与有效期 */
    boolean validateToken(String token);
}
```

### 2. `JwtTokenService`（默认实现）

默认实现基于 JJWT，token Claims 携带：

- `userId`、`username`、`userType`（枚举 code）
- `shopId`（商家登录时有值）
- `permissionCodes`（权限码列表）
- `extra`（扩展字段，原样合并进 Claims）

### 3. `AuthTokenException`

`parseToken` 解析失败（签名无效、过期、格式错误）统一抛出 `AuthTokenException`，携带原始 token 便于日志排查。

## 配置项

```yaml
jwt:
  secret: cloud-mall-jwt-secret-key-2026-very-long-secret-key  # HMAC-SHA 签名密钥，生产必须覆盖
  expiration: 86400000                                          # 过期时间（毫秒），默认 24 小时
```

## 使用方式

引入依赖后直接注入接口，无需关注实现细节：

```java
@RequiredArgsConstructor
public class AuthApp {

    private final ITokenService tokenService;

    public LoginResp login(LoginReq req) {
        UserContext ctx = UserContext.builder()
                                     .userId(user.getId())
                                     .username(user.getUsername())
                                     .userType(user.getUserType())
                                     .build();
        String token = tokenService.createToken(ctx);
        return LoginResp.builder().token(token).build();
    }
}
```

## 自动装配

`JwtAutoConfiguration` 通过 `AutoConfiguration.imports` 自动注册 `ITokenService` Bean（`@ConditionalOnMissingBean`）。需要替换实现（如 OAuth2、Session）时，注册自定义 `ITokenService` Bean 即可覆盖。

## 依赖关系

```
cloudmall-jwt-spring-boot-starter
├── cloudmall-common（UserContext）
├── jjwt-api / jjwt-impl / jjwt-jackson
└── hutool-core
```

## 注意事项

- `jwt.secret` 用于 HMAC-SHA 密钥推导，长度必须满足签名算法要求（至少 32 字节）；修改密钥后所有旧 token 立即失效，生产环境务必通过环境变量覆盖默认值。
- 该 starter 只负责令牌生成与解析，不包含登录态校验过滤器（鉴权统一在 Gateway 完成）。
