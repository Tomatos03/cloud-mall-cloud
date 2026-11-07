# cloudmall-jwt-spring-boot-starter

JWT 认证令牌 starter，基于 JJWT（HMAC-SHA 签名），提供令牌创建、解析、校验三大能力，并抽象为 `ITokenService` 接口便于替换实现。

## 功能

### 1. `ITokenService` 接口

```java
public interface ITokenService {

    /** 创建令牌：将声明集合编码为 token */
    String createToken(Map<String, Object> claims);

    /** 解析令牌：校验签名与有效期，返回只读的 ParsedToken，失败抛 ParseTokenException */
    ParsedToken parseToken(String token);

    /** 校验令牌：解析成功返回 true，失败返回 false */
    default boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ParseTokenException e) {
            return false;
        }
    }
}
```

### 2. `ParsedToken`

`parseToken` 的返回值，只读包装已校验的 `Claims`，提供：

- `subject()` / `ensureSubject(token)`：用户标识（优先标准 `sub`，兼容旧 token 的 `userId`）
- `id()`：token 唯一标识（jti）
- `getString(key)` / `getLong(key)` / `getStringList(key)` / `getMap(key)` / `get(key, type)`
- `issuedAt()` / `expiration()`

构造器为包级可见，只允许 `JwtTokenService` 创建，避免包装未经校验的 `Claims`。

### 3. `JwtTokenService`（默认实现）

基于 JJWT，声明由调用方通过 `Map` 传入，常用键名见 `JwtClaimNames`：

- `userId`、`username`、`userType`（枚举 code）
- `shopId`（商家登录时有值）
- `permissionCodes`（权限码列表）

值为 `null` 的声明会被忽略；未识别的键原样写入 Claims，便于扩展。

### 4. `ParseTokenException`

`parseToken` 解析失败（签名无效、过期、格式错误、缺少用户标识）统一抛出 `ParseTokenException`，携带原始 token 便于日志排查。

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

    public String login(UserDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimNames.USER_ID, user.getId());
        claims.put(JwtClaimNames.USERNAME, user.getUsername());
        claims.put(JwtClaimNames.USER_TYPE, user.getTypes());
        return tokenService.createToken(claims);
    }
}
```

解析侧：

```java
ParsedToken parsedToken = tokenService.parseToken(token);
Long userId = parsedToken.getLong(JwtClaimNames.USER_ID);
String username = parsedToken.getString(JwtClaimNames.USERNAME);
```

## 自动装配

`JwtAutoConfiguration` 通过 `AutoConfiguration.imports` 自动注册 `ITokenService` Bean（`@ConditionalOnMissingBean`）。需要替换实现（如 OAuth2、Session）时，注册自定义 `ITokenService` Bean 即可覆盖。

## 依赖关系

```
cloudmall-jwt-spring-boot-starter
├── spring-boot-starter
├── hutool-core
└── jjwt-api / jjwt-impl / jjwt-jackson
```

## 注意事项

- `jwt.secret` 用于 HMAC-SHA 密钥推导，长度必须满足签名算法要求（至少 32 字节）；修改密钥后所有旧 token 立即失效，生产环境务必通过环境变量覆盖默认值。
- 该 starter 只负责令牌生成与解析，不包含登录态校验过滤器（鉴权统一在 Gateway 完成）。
- starter 不再依赖 `cloudmall-common`，解析结果以 `ParsedToken` 暴露；需要 `UserContext` 的模块自行完成映射。
