#!/usr/bin/env bash
# 秒杀 QPS 对比压测运行脚本
# 用法: ./run.sh [并发数] [重复轮数] [重新生成数据 1/0] [用户数] [持续时长s]
#   [持续时长s] 缺省 0 = 一次性模板（每线程 1 次，测延迟）；
#               大于 0 = 持续压测模板（每线程循环发送 DURATION 秒，测服务端吞吐 QPS）
# 默认每轮: 重置数据 -> 跑异步 -> 重置 -> 跑普通；首轮前可重新生成压测数据
set -e
cd "$(dirname "$0")"

THREADS="${1:-10}"
ROUNDS="${2:-3}"
REGEN="${3:-1}"   # 1=压测前重新生成活动+用户池, 0=沿用现有数据
USER_COUNT="${4:-200}"   # 生成用户池大小（每个 CSV），默认 200
DURATION="${5:-0}"       # 持续压测时长（秒），0 = 一次性模板
RESULT_DIR="results"
# 每次运行前清理旧结果（jmx 用 -f 覆盖同名文件，这里清掉历史轮次残留）
rm -rf "$RESULT_DIR"
mkdir -p "$RESULT_DIR"

# 项目根: 脚本位于 <项目根>/benchmark/seckill-purchase，向上两级
PROJECT_ROOT="$(cd ../.. && pwd)"
JMETER_DIR="$(pwd)"

# 选择模板：持续模式用 -cont.jmx（loop=-1 + 固定时长），一次性模式用基础 jmx（loop=1）
async_jmx="seckill-async.jmx"
plain_jmx="seckill-plain.jmx"
jmeter_duration_arg=()
if [ "$DURATION" -gt 0 ]; then
  async_jmx="seckill-async-cont.jmx"
  plain_jmx="seckill-plain-cont.jmx"
  jmeter_duration_arg=(-JDURATION="$DURATION")
  echo "模式: 持续压测 ${DURATION}s（测服务端吞吐）"
else
  echo "模式: 一次性并发（每线程 1 次，测延迟）"
fi

generate_data() {
  echo ""
  echo "=== [1/3] 重新初始化秒杀压测活动（SeckillPressDataInitTest）==="
  # 重建活动 900001（RUNNING，2 小时窗口）并预热 Redis 库存
  (cd "$PROJECT_ROOT" && mvn test -pl services/seckill-service -am \
    -Dtest=SeckillPressDataInitTest -Dsurefire.failIfNoSpecifiedTests=false \
    -q 2>&1 | grep -E "Tests run:|BUILD FAILURE" | tail -3)

  echo "=== [2/3] 生成用户池（press_ 前缀 -> seckill-users.csv, ${USER_COUNT} 用户）==="
  # 前缀固定 press_，async/plain 用户池内容一致，只需生成一次再复制，
  # 省去一次 Spring Boot 上下文启动（大数据量下最大固定开销）
  (cd "$PROJECT_ROOT" && mvn test -pl services/user-service -am \
    -Dtest=BatchRegisterAppTest -Dsurefire.failIfNoSpecifiedTests=false \
    -Dbatch.output.dir="$JMETER_DIR" \
    -Dbatch.output.file=seckill-users.csv \
    -Dbatch.user.count="$USER_COUNT" \
    -q 2>&1 | grep -E "Tests run:|BUILD FAILURE" | tail -3)

  echo "=== [3/3] 复制用户池为 async/plain 两份 ==="
  cd "$JMETER_DIR"
  cp -f seckill-users.csv seckill-users-async.csv
  cp -f seckill-users.csv seckill-users-plain.csv

  echo "=== 用户池 CSV 已生成==="
  for f in seckill-users-async.csv seckill-users-plain.csv; do
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
echo "=== 秒杀 QPS 对比压测: 并发=${THREADS}, 轮数=${ROUNDS}, 持续时长=${DURATION}s ==="

# 统计函数：解析 jtl（CSV），输出 请求总数 成功数 失败数 吞吐(req/s) 平均耗时(ms) 最小耗时(ms) 最大耗时(ms) 错误率(%)
stats_jtl() {
  awk -F, -v dur="$DURATION" '
    NR == 1 { next }
    NR == 2 { first = $1; min = $2; max = $2 }
    {
      last = $1; total++; if ($8 == "true") ok++; sum += $2
      if ($2 < min) min = $2
      if ($2 > max) max = $2
    }
    END {
      span = (last - first) / 1000
      # 时间戳倒挂（last < first）或跨度异常大（脏数据）时吞吐无意义
      if (last < first || (dur > 0 && span > dur * 3)) {
        printf "%d %d %d %s %.1f %.1f %.1f %.1f", total, ok, total - ok, "N/A", sum / total, min, max, (total - ok) * 100 / total
      } else {
        if (span <= 0) span = 1
        printf "%d %d %d %.1f %.1f %.1f %.1f %.1f", total, ok, total - ok, total / span, sum / total, min, max, (total - ok) * 100 / total
      }
    }' "$1"
}

printf "\n%-6s %-6s %10s %8s %10s %12s %12s %10s %10s %8s\n" "轮次" "方案" "请求总数" "成功数" "失败数" "吞吐(req/s)" "平均耗时(ms)" "最小耗时" "最大耗时" "错误率"
printf "%-6s %-6s %10s %8s %10s %12s %12s %10s %10s %8s\n" "------" "------" "----------" "--------" "----------" "------------" "------------" "----------" "----------" "--------"

for round in $(seq 1 "$ROUNDS"); do
  # 异步方案
  reset_data
  jmeter -n -f -t "$async_jmx" -JTHREADS="$THREADS" -JRAMP=2 \
    "${jmeter_duration_arg[@]}" \
    -l "$RESULT_DIR/async-r${round}.jtl" >/dev/null 2>&1
  read -r a_total a_ok a_fail a_qps a_avg a_min a_max a_err <<<"$(stats_jtl "$RESULT_DIR/async-r${round}.jtl")"
  printf "%-6s %-6s %10s %8s %10s %12s %12s %10s %10s %8s\n" "第${round}轮" "异步" "$a_total" "$a_ok" "$a_fail" "$a_qps" "$a_avg" "${a_min}ms" "${a_max}ms" "${a_err}%"

  # 普通方案
  reset_data
  jmeter -n -f -t "$plain_jmx" -JTHREADS="$THREADS" -JRAMP=2 \
    "${jmeter_duration_arg[@]}" \
    -l "$RESULT_DIR/plain-r${round}.jtl" >/dev/null 2>&1
  read -r p_total p_ok p_fail p_qps p_avg p_min p_max p_err <<<"$(stats_jtl "$RESULT_DIR/plain-r${round}.jtl")"
  printf "%-6s %-6s %10s %8s %10s %12s %12s %10s %10s %8s\n" "第${round}轮" "普通" "$p_total" "$p_ok" "$p_fail" "$p_qps" "$p_avg" "${p_min}ms" "${p_max}ms" "${p_err}%"
done

