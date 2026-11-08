# IK 分词插件（search-service 依赖）

商品搜索索引 mapping 使用 `ik_max_word`（索引）/ `ik_smart`（搜索）分析器。
本目录提供 **IK 8.15.5 离线包**，compose 通过目录挂载让 ES 启动时直接加载插件，
**不依赖在线安装**（官方 CDN 拉取不稳定）。

## 目录内容

| 文件/目录 | 说明 |
|-----------|------|
| `elasticsearch-analysis-ik-8.15.5.zip` | IK 8.15.5 离线安装包（官方 CDN 下载，4.6MB） |
| `ik/` | 解压后的完整插件目录（`plugin-descriptor.properties` + `config/` + 6 个 jar），compose 挂载目标 |
| `README.md` | 本文档 |

> `docs/docker/es-plugins/ik/*.jar` 在仓库 `.gitignore` 中做了例外保留，确保 clone 后 bind mount 可直接启动 ES；不要只拷贝 `config/` 和描述文件，否则 ES 会报 `Could not find plugin class [com.infinilabs.ik.elasticsearch.AnalysisIkPlugin]`。

## 加载机制（compose.yaml）

`elasticsearch` 服务通过 bind mount 直接把插件目录挂进 ES：

```yaml
volumes:
  - ./es-plugins/ik:/usr/share/elasticsearch/plugins/ik
```

ES 启动时自动扫描 `plugins/ik` 加载 analysis-ik，无需 `elasticsearch-plugin install`。

## 更新插件版本

```bash
# 1. 下载新版本 zip（官方源：https://get.infini.cloud/elasticsearch/analysis-ik/{版本}）
curl -L -o es-plugins/elasticsearch-analysis-ik-<版本>.zip \
  https://get.infini.cloud/elasticsearch/analysis-ik/<版本>

# 2. 校验并解压
unzip -t es-plugins/elasticsearch-analysis-ik-<版本>.zip
rm -rf es-plugins/ik && mkdir -p es-plugins/ik
unzip -q es-plugins/elasticsearch-analysis-ik-<版本>.zip -d es-plugins/ik

# 3. 重启 ES 容器
docker compose -f docs/docker/compose.yaml up -d elasticsearch
```

## 验证

```bash
# 插件已加载（输出应包含 analysis-ik 8.15.5）
curl http://localhost:9200/_cat/plugins

# IK 分词（ik_max_word 应输出 苹果 / 手机 等中文词元，而非单字）
curl -X POST 'http://localhost:9200/_analyze' \
  -H 'Content-Type: application/json' \
  -d '{"analyzer": "ik_max_word", "text": "苹果手机"}'
```
