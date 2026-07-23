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
	CONFIG_DIR="/config"
fi

echo "Nacos: ${BASE_URL}"
echo "Config dir: ${CONFIG_DIR}"
echo "Group: ${GROUP}"
echo ""

echo "Waiting for Nacos to be ready..."
for i in $(seq 1 30); do
	if curl -sf "${BASE_URL}/nacos/" -o /dev/null 2>/dev/null; then
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

# 扩展名 → Nacos type 映射
resolve_type() {
	case "$1" in
	yaml | yml) echo "yaml" ;;
	properties) echo "Properties" ;;
	json) echo "JSON" ;;
	xml) echo "XML" ;;
	html) echo "HTML" ;;
	txt | text) echo "TEXT" ;;
	toml) echo "TOML" ;;
	*) echo "" ;;
	esac
}

SUCCESS=0
FAIL=0

for FILE in "${CONFIG_DIR}"/*; do
	[ -f "$FILE" ] || continue

	NAME=$(basename "$FILE")
	EXT="${NAME##*.}"
	TYPE=$(resolve_type "$EXT")

	if [ -z "$TYPE" ]; then
		echo "  SKIP ${NAME} (unsupported extension .${EXT})"
		continue
	fi

	DATA_ID="${NAME%.*}"

	echo "Publishing ${DATA_ID} (${TYPE}) ..."

	HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
		-X POST "${BASE_URL}/nacos/v1/cs/configs" \
		-d "tenant=" \
		-d "dataId=${DATA_ID}" \
		-d "group=${GROUP}" \
		-d "type=${TYPE}" \
		--data-urlencode "content@${FILE}")

	if [ "$HTTP_CODE" = "200" ]; then
		echo "  OK  ${DATA_ID}"
		SUCCESS=$((SUCCESS + 1))
	else
		echo "  FAIL ${DATA_ID} (HTTP ${HTTP_CODE})"
		FAIL=$((FAIL + 1))
	fi
done

echo ""
echo "Done. Published: ${SUCCESS}, Failed: ${FAIL}"
[ "$FAIL" -eq 0 ]
