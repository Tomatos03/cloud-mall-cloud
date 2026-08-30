-- 固定窗口限流：原子判断 key 是否存在并计数
-- KEYS[1] = 请求计数 key (seckill:rate:{userId})
-- ARGV[1] = 窗口过期时间（秒）
-- ARGV[2] = 每分钟请求上限
-- 返回：当前计数值（首次请求为1，超限时仍返回递增后的值）
local key = KEYS[1]
local windowSeconds = tonumber(ARGV[1])
local maxRequests = tonumber(ARGV[2])

local exists = redis.call('EXISTS', key)
if exists == 1 then
    local count = redis.call('INCR', key)
    return count
else
    redis.call('SET', key, '1', 'EX', windowSeconds)
    return 1
end
