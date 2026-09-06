local key = KEYS[1]
local nowMillis = tonumber(ARGV[1])
local maxRequests = tonumber(ARGV[2])
local windowSeconds = tonumber(ARGV[3])
local memberSuffix = ARGV[4]

local windowStart = nowMillis - (windowSeconds * 1000)

redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

local count = redis.call('ZCARD', key)

if count < maxRequests then
    local member = tostring(nowMillis) .. "-" .. memberSuffix
    redis.call('ZADD', key, nowMillis, member)
    redis.call('EXPIRE', key, windowSeconds * 2)
    return {1, maxRequests - count - 1, windowSeconds}
else
    redis.call('EXPIRE', key, windowSeconds * 2)
    return {0, 0, windowSeconds}
end