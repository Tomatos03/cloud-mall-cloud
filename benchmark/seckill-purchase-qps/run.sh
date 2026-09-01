#!/usr/bin/env bash
# 秒杀 QPS 梯度扫描脚本：并发从低到高递增，持续压测每个档位，找出 QPS 拐点。
# 用法: ./run.sh [起点并发] [终点并发] [步长] [单点时长s] [用户数] [重生成数据 1/0]
# 默认: ./run.sh 10 100 10 20 200 1
set -e
cd "$(dirname "$0")"

START="${1:-10}"
END="${2:-100}"
STEP="${3:-10}"
DURATION="${4:-20}"
USER_COUNT="${5:-200}"
REGEN="${6:-1}"   # 1=压测前重新生成活动+用户池, 0=沿用现有数据

RESULT_DIR="results"
# 每次运行前清理旧结果（jmx 用 -f 覆盖同名文件，这里清掉历史档位残留）
rm -rf "$RESULT_DIR"
mkdir -p "$RESULT_DIR"

# 项目根: 脚本位于 <项目根>/benchmark/seckill-purchase-qps，向上两级
PROJECT_ROOT="$(cd ../.. && pwd)"
QPS_DIR="$(pwd)"

generate_data() {
  echo ""
  echo "=== [1/3] 重新初始化秒杀压测活动（SeckillPressDataInitTest）==="
  (cd "$PROJECT_ROOT" && mvn test -pl services/seckill-service -am \
    -Dtest=SeckillPressDataInitTest -Dsurefire.failIfNoSpecifiedTests=false \
    -q 2>&1 | grep -E "Tests run:|BUILD FAILURE" | tail -3)

  echo "=== [2/3] 生成用户池（press_ 前缀 -> qps-users.csv, ${USER_COUNT} 用户）==="
  # 前缀固定 press_，async/plain 用户池内容一致，只需生成一次再复制，
  # 省去一次 Spring Boot 上下文启动（大数据量下最大固定开销）
  (cd "$PROJECT_ROOT" && mvn test -pl services/user-service -am \
    -Dtest=BatchRegisterAppTest -Dsurefire.failIfNoSpecifiedTests=false \
    -Dbatch.output.dir="$QPS_DIR" \
    -Dbatch.output.file=qps-users.csv \
    -Dbatch.user.count="$USER_COUNT" \
    -q 2>&1 | grep -E "Tests run:|BUILD FAILURE" | tail -3)

  echo "=== [3/3] 复制用户池为 async/plain 两份 ==="
  cd "$QPS_DIR"
  cp -f qps-users.csv qps-users-async.csv
  cp -f qps-users.csv qps-users-plain.csv

  echo "=== 用户池 CSV 已生成 ==="
  for f in qps-users-async.csv qps-users-plain.csv; do
    echo "  $f: $(wc -l < "$f") 个用户"
  done
}

reset_data() {
  # 清秒杀单（异步方案落 cm_seckill_order）
  docker exec cm-mysql mysql -uroot -proot cm_mall \
    -e "DELETE FROM cm_seckill_order WHERE activity_id=900001;" >/dev/null 2>&1
  # 清普通方案生成的秒杀真实订单（order_no 以 SK 开头）
  docker exec cm-mysql mysql -uroot -proot cm_mall \
    -e "DELETE FROM cm_order WHERE order_no LIKE 'SK%';" >/dev/null 2>&1
  # 清两种一人一单标记（异步 seckill:user: / 普通 seckill:plain:user:），
  # 分批 DEL 避免大数据量下单次参数超限
  docker exec cm-redis redis-cli --scan --pattern 'seckill:user:900001:*' 2>/dev/null \
    | xargs -r -n 500 docker exec cm-redis redis-cli DEL >/dev/null 2>&1
  docker exec cm-redis redis-cli --scan --pattern 'seckill:plain:user:*' 2>/dev/null \
    | xargs -r -n 500 docker exec cm-redis redis-cli DEL >/dev/null 2>&1
  # 重置库存
  docker exec cm-redis redis-cli MSET \
    seckill:stock:900001:1 100000 \
    seckill:stock:900001:2 100000 \
    seckill:stock:900001:3 100000 >/dev/null 2>&1
}

if [ "$REGEN" = "1" ]; then
  generate_data
fi

echo ""
echo "=== 秒杀 QPS 梯度扫描: 并发 ${START} -> ${END} (步长 ${STEP}), 单点时长 ${DURATION}s ==="

# 梯度扫描：对每个并发档位，分别压异步与普通（每档位前重置，避免一人一单耗尽）
THREADS="$START"
while [ "$THREADS" -le "$END" ]; do
  echo ""
  echo "--- 并发档位: ${THREADS} ---"

  # 异步
  reset_data
  echo "[异步@${THREADS}] 重置完成，持续压测 ${DURATION}s..."
  jmeter -n -f -t qps-async-cont.jmx -JTHREADS="$THREADS" -JDURATION="$DURATION" -JRAMP=2 \
    -l "$RESULT_DIR/async-t${THREADS}.jtl" 2>&1 | grep "summary =" | tail -1 || true

  # 普通
  reset_data
  echo "[普通@${THREADS}] 重置完成，持续压测 ${DURATION}s..."
  jmeter -n -f -t qps-plain-cont.jmx -JTHREADS="$THREADS" -JDURATION="$DURATION" -JRAMP=2 \
    -l "$RESULT_DIR/plain-t${THREADS}.jtl" 2>&1 | grep "summary =" | tail -1 || true

  THREADS=$((THREADS + STEP))
done

echo ""
echo "=== 汇总与拐点识别 ==="
# 用 awk 解析 jtl（CSV）：列序 timeStamp,elapsed,...,success(第8列),...
# 输出: 总请求 成功 总QPS(req/s) 成功QPS(req/s) 平均耗时ms 失败率%
summarize() {
  awk -F, -v dur="$DURATION" '
    NR == 1 { next }
    NR == 2 { first = $1 }
    { last = $1; total++; if ($8 == "true") ok++; sum += $2 }
    END {
      span = (last - first) / 1000
      if (span > dur * 3) { print "INVALID"; exit }
      if (span <= 0) span = 1
      printf "%d %d %.1f %.1f %.1f %.1f", total, ok, total / span, ok / span, sum / total, (total - ok) * 100 / total
    }' "$1"
}

for scheme in async plain; do
  echo ""
  echo "[${scheme}] 并发 -> 总QPS(req/s) / 成功QPS(req/s) / 失败率 / 平均耗时"
  threads_list=()
  qps_list=()
  okqps_list=()
  failrate_list=()
  avg_list=()
  for f in $(ls "$RESULT_DIR"/${scheme}-t*.jtl 2>/dev/null | sort -V); do
    t=$(basename "$f" | sed 's/.*-t\([0-9]*\)\.jtl/\1/')
    read -r total ok qps ok_qps avg_ms fail_rate <<<"$(summarize "$f")"
    if [ "$total" = "INVALID" ]; then
      echo "  ${t}: jtl 时间跨度异常（疑似脏数据），跳过"
      continue
    fi
    threads_list+=("$t")
    qps_list+=("$qps")
    okqps_list+=("$ok_qps")
    failrate_list+=("$fail_rate")
    avg_list+=("$avg_ms")
    printf "  %5s -> %8.1f req/s | 成功 %7.1f req/s | 失败率 %5.1f%% | 平均 %6.1fms\n" "$t" "$qps" "$ok_qps" "$fail_rate" "$avg_ms"
  done

  # 拐点识别：平台期起点 = 增速首次 <10% 且此前增速 >30%；峰值 = 总 QPS 最大档位
  n=${#threads_list[@]}
  if [ "$n" -ge 3 ]; then
    plateau=""
    prev_growth=""
    for ((i = 1; i < n; i++)); do
      prev_qps=${qps_list[$((i - 1))]}
      cur_qps=${qps_list[$i]}
      growth=$(awk -v p="$prev_qps" -v c="$cur_qps" 'BEGIN { if (p > 0) printf "%.1f", (c - p) * 100 / p; else print 0 }')
      if [ -z "$plateau" ] && [ -n "$prev_growth" ] && \
         awk -v g="$growth" -v pg="$prev_growth" 'BEGIN { exit !(pg > 30 && g < 10) }'; then
        plateau="${threads_list[$i]}"
      fi
      prev_growth="$growth"
    done
    peak="${threads_list[0]}"
    peak_qps="${qps_list[0]}"
    for ((i = 1; i < n; i++)); do
      if awk -v c="${qps_list[$i]}" -v p="$peak_qps" 'BEGIN { exit !(c > p) }'; then
        peak="${threads_list[$i]}"
        peak_qps="${qps_list[$i]}"
      fi
    done
    if [ -n "$plateau" ]; then
      echo "  >> [${scheme}] 吞吐平台期约从并发 ${plateau} 开始（增速显著放缓），峰值在并发 ${peak}"
    else
      echo "  >> [${scheme}] 未检测到明显平台期，可扩大并发范围或缩小步长重扫；当前峰值在并发 ${peak}"
    fi
  fi
done

