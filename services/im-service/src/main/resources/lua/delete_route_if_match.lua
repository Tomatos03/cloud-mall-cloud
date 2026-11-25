-- 仅当路由仍属于指定实例与会话时才删除，避免旧连接关闭误删新连接
-- KEYS[1] = im:route:{userId}
-- ARGV[1] = sessionId（hash field）
-- ARGV[2] = instanceId
-- 返回：1=已删除，0=未删除（实例不匹配或字段不存在）
local raw = redis.call('HGET', KEYS[1], ARGV[1])
if not raw then
    return 0
end
local ok, route = pcall(cjson.decode, raw)
if ok and route['instanceId'] == ARGV[2] then
    redis.call('HDEL', KEYS[1], ARGV[1])
    if redis.call('HLEN', KEYS[1]) == 0 then
        redis.call('DEL', KEYS[1])
    end
    return 1
end
return 0
