# cloudmall-elasticsearch-spring-boot-starter

Elasticsearch 客户端自动装配 starter，基于官方 `elasticsearch-java` 客户端（8.15.5），提供开箱即用的 `ElasticsearchClient` Bean。

## 功能

- **客户端自动装配**：`RestClient → RestClientTransport → ElasticsearchClient`，JSON 序列化使用 Jackson（`JacksonJsonpMapper`）
- **多节点支持**：`uris` 逗号分隔，自动解析为多个 `HttpHost`
- **基础认证**：配置 `username` / `password` 后自动注入 Basic Credentials
- **超时控制**：连接超时与 Socket 超时可配置

## 配置项

```yaml
cloudmall:
  elasticsearch:
    enabled: true                  # 总开关，false 不装配客户端（默认 true）
    uris: http://localhost:9200    # 连接地址，多个逗号分隔
    username: elastic              # 用户名（可选）
    password: ""                   # 密码（可选）
    connect-timeout: 5000          # 连接超时（毫秒）
    socket-timeout: 60000          # Socket 超时（毫秒）
```

## 使用方式

引入依赖后直接注入客户端：

```java
@Service
@RequiredArgsConstructor
public class ProductIndexRepository {

    private final ElasticsearchClient client;

    public void index(ProductIndexDoc doc) throws IOException {
        client.index(i -> i.index(PRODUCT_INDEX).id(doc.getId()).document(doc));
    }
}
```

## 自动装配

`ElasticsearchAutoConfiguration` 装配条件：

- classpath 存在 `ElasticsearchClient`
- `cloudmall.elasticsearch.enabled != false`（默认启用）
- 消费方未自定义 `ElasticsearchClient` Bean（`@ConditionalOnMissingBean`）

## 依赖关系

```
cloudmall-elasticsearch-spring-boot-starter
├── elasticsearch-java / elasticsearch-rest-client（官方客户端 8.15.5）
├── jackson-databind / jackson-datatype-jsr310（JSON 序列化）
└── spring-boot-autoconfigure
```

## 注意事项

- 该 starter 只负责客户端装配，不包含索引管理、数据同步等业务能力（见 search-service 的索引初始化与增量同步任务）。
- 生产环境建议通过环境变量注入 `uris` 与账号密码，避免明文配置。
