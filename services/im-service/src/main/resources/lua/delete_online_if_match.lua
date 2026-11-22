-- 仅当在线标记仍属于指定会话时才删除，避免旧会话关闭回调误删重连后的新会话
-- KEYS[1] = 在线状态 key (im:online:{userId})
-- ARGV[1] = 期望的 sessionId
-- 返回：1=已删除，0=未删除（sessionId 不匹配或 key 不存在）
if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('DEL', KEYS[1])
end
return 0
