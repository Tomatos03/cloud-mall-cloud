# Nacos 配置同步

> Nacos Server 版本：3.0.3（与 Spring Cloud Alibaba 2025.0.0.0 配套）

## 前提

- 已通过 `docker compose up -d nacos` 启动 Nacos
- `docs/nacos/config/` 下存放了所有服务的配置

## 手动同步

修改 `docs/nacos/config/*.yaml` 后执行：

```bash
./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config
```

### 参数

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `NACOS_PORT` | `8848` | Nacos API 端口 |
| `GROUP` | `DEFAULT_GROUP` | 配置分组 |

位置参数：

```bash
./docs/docker/sh/sync-nacos-config.sh <host> [config-dir]
```

## 自动同步

`docker compose up` 启动时通过 `nacos-init` 容器自动同步，无需手动操作。

## 验证

登录 Nacos 控制台 `http://localhost:8080`，进入「配置管理 → 配置列表」，确认各 Data ID 内容正确。
