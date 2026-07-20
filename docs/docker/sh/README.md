# Nacos 配置同步

## 前提

- 已通过 `docker compose up -d nacos` 启动 Nacos（localhost:8848）
- `docs/nacos/config/` 下存放了所有服务的配置

## 手动同步

脚本默认配置目录为 Docker 容器内的 `/config`，**本地运行时必须显式指定配置目录**。

修改 `docs/nacos/config/*.yaml` 后，执行：

```bash
./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config
```

### 指定主机

```bash
# 远程 Nacos
./docs/docker/sh/sync-nacos-config.sh 192.168.1.100 docs/nacos/config

# 指定端口
NACOS_PORT=8849 ./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config
```

### 指定分组

```bash
GROUP=DEV_GROUP ./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config
```

### 指定其他目录

```bash
./docs/docker/sh/sync-nacos-config.sh localhost ./my-configs/
```

## 自动同步

`docker compose` 启动时会通过 `nacos-init` 容器自动执行同步，无需手动操作。

## 验证

登录 Nacos 控制台查看已发布的配置：

```
http://localhost:8848/nacos
```

进入「配置管理 → 配置列表」，确认对应服务的 Data ID 内容正确。
