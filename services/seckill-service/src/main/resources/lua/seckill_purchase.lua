-- 秒杀抢购原子脚本：一人一单 + 库存校验扣减
-- KEYS[1] = 库存 key (seckill:stock:{activityId}:{skuId})
-- KEYS[2] = 一人一单标记 key (seckill:user:{activityId}:{skuId}:{userId})
-- ARGV[1] = 购买数量（固定 1）
-- ARGV[2] = 一人一单标记 TTL（秒）
-- 返回：1=成功，-2=已抢购，-3=库存不足
local userKey = KEYS[2]
if redis.call('EXISTS', userKey) == 1 then
    return -2
end
local stockKey = KEYS[1]
local stock = redis.call('GET', stockKey)
if not stock or tonumber(stock) < tonumber(ARGV[1]) then
    return -3
end
redis.call('DECRBY', stockKey, ARGV[1])
redis.call('SET', userKey, '1', 'EX', ARGV[2])
return 1
