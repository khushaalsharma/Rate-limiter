local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

-- First request for this client: initialize full bucket
if tokens == nil then
    tokens = capacity
    last_refill = now
end

-- Refill tokens based on time elapsed since last refill
local elapsed = now - last_refill
local refilled = elapsed * refill_rate
tokens = math.min(capacity, tokens + refilled)
last_refill = now

local reset_after = math.ceil((1 - (tokens % 1)) / refill_rate)

if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
    redis.call('EXPIRE', key, math.ceil(capacity / refill_rate) + 60)
    return {1, math.floor(tokens), reset_after}
else
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
    redis.call('EXPIRE', key, math.ceil(capacity / refill_rate) + 60)
    return {0, 0, reset_after}
end