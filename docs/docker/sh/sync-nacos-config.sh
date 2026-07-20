#!/bin/bash
# 将 docs/nacos/config/ 下的配置批量发布到 Nacos
# 用法: ./sync-nacos-config.sh [nacos-host] [config-dir]

set -euo pipefail

NACOS_HOST="${1:-localhost}"
NACOS_PORT="${NACOS_PORT:-8848}"
BASE_URL="http://${NACOS_HOST}:${NACOS_PORT}"
GROUP="${GROUP:-DEFAULT_GROUP}"

if [ -n "${2:-}" ]; then
  CONFIG_DIR="$2"
else
  # 容器中运行时配置挂载在 /config
  CONFIG_DIR="/config"
fi

echo "Nacos: ${BASE_URL}"
echo "Config dir: ${CONFIG_DIR}"
echo "Group: ${GROUP}"
echo ""

# 等待 Nacos 就绪
echo "Waiting for Nacos to be ready..."
for i in $(seq 1 30); do
  if curl -sf "${BASE_URL}/nacos/v2/console/health/liveness" -o /dev/null 2>/dev/null; then
    echo "Nacos is ready."
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "ERROR: Nacos did not become ready within 30s."
    exit 1
  fi
  sleep 2
done
echo ""

# 遍历发布配置
SUCCESS=0
FAIL=0
for FILE in "${CONFIG_DIR}"/*.yaml; do
  NAME=$(basename "$FILE")
  DATA_ID="${NAME}"
  CONTENT=$(cat "$FILE")

  echo "Publishing ${DATA_ID} ..."

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -d "dataId=${DATA_ID}" \
    -d "group=${GROUP}" \
    -d "type=yaml" \
    --data-urlencode "content=${CONTENT}" \
    -X POST "${BASE_URL}/nacos/v2/cs/config")

  # v2 API returns 200 on success
  if [ "$HTTP_CODE" = "200" ]; then
    echo "  ✓ ${DATA_ID}"
    SUCCESS=$((SUCCESS + 1))
  else
    echo "  ✗ ${DATA_ID} (HTTP ${HTTP_CODE})"
    FAIL=$((FAIL + 1))
  fi
done

echo ""
echo "Done. Published: ${SUCCESS}, Failed: ${FAIL}"
[ "$FAIL" -eq 0 ]
