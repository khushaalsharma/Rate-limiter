# Distributed Rate Limiter as a Service

A production-grade distributed rate limiting microservice built with Java 17 and Spring Boot. Supports three rate limiting algorithms, Redis-backed state management, async analytics via Kafka, and a fully containerised setup with Docker Compose.

---

## Why This Exists

Rate limiting is a core infrastructure problem every backend system eventually faces — protecting APIs from DDoS attacks, preventing resource exhaustion, enforcing fair usage across clients. This project implements it as a standalone microservice that any upstream service can call before processing a request.

---

## Architecture

```
Incoming Request
      │
      ▼
┌─────────────────────────────┐
│   Rate Limiter Service       │
│   (Spring Boot 3.2)          │
│                              │
│  1. Load config → Postgres   │
│  2. Check state → Redis      │  ← hot path (sub-millisecond)
│  3. Allow / Deny             │
│  4. Publish event → Kafka    │  ← async, never blocks response
└─────────────────────────────┘
         │              │
         ▼              ▼
      Redis           Kafka
   (rate state)   (event stream)
                       │
                       ▼
                 Analytics Consumer
                       │
                       ▼
                  PostgreSQL
              (events + configs)
```

**Core design decisions:**

- Redis is the sole source of truth for rate limit state — no database call on the critical path
- Kafka decouples analytics persistence from the allow/deny decision — a slow or down database never affects response latency
- Token bucket uses a Redis Lua script for atomic check-and-decrement — no distributed locks needed
- Strategy pattern across all three algorithms — adding a new algorithm is one class + one enum value

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Rate limit state | Redis 7 |
| Event streaming | Apache Kafka |
| Database | PostgreSQL 15 |
| Containerisation | Docker + Docker Compose |

---

## How the Three Algorithms Work

### Fixed Window
Divides time into fixed buckets (e.g. every 60 seconds). Counts requests per bucket using Redis `INCR` with a TTL. Fast and memory-efficient, but vulnerable to boundary bursts — a client can send 2x the limit by hitting the last second of one window and the first second of the next.

```
Redis key:  rl:fixed:{clientKey}:{windowTimestamp}
Structure:  String (counter)
Commands:   INCR → EXPIRE (on first request)
```

### Sliding Window
Fixes the boundary burst problem by maintaining a rolling window that ends exactly at the current moment. Uses a Redis Sorted Set where each member is a request timestamp. On every request — remove timestamps outside the window, count what remains, add current timestamp if under limit.

```
Redis key:  rl:sliding:{clientKey}
Structure:  Sorted Set (score = epoch millis)
Commands:   ZREMRANGEBYSCORE → ZCARD → ZADD
```

### Token Bucket
Each client has a bucket with a maximum capacity of N tokens. Tokens refill at a fixed rate. Each request consumes one token. Allows controlled bursting — a client can use saved-up tokens for a brief spike — while enforcing a sustained rate cap. The entire check-refill-decrement sequence runs as an atomic Lua script on Redis, preventing race conditions without locks.

```
Redis key:  rl:token:{clientKey}
Structure:  Hash (tokens, last_refill)
Commands:   Lua script (HMGET → compute refill → HMSET atomically)
```

### Algorithm Comparison

| Algorithm | Redis Structure | Burst Handling | Memory | Best For |
|---|---|---|---|---|
| Fixed Window | String | Vulnerable at boundaries | O(1) | Internal services, simple limits |
| Sliding Window | Sorted Set | Accurate, no bursting | O(requests) | Public APIs needing precision |
| Token Bucket | Hash + Lua | Allows controlled bursting | O(1) | Production APIs, user-facing rate limits |

---

## Quick Start

**Prerequisites:** Docker, Java 17, Maven

```bash
# Clone the repo
git clone https://github.com/yourusername/rate-limiter-service
cd rate-limiter-service

# Start all infrastructure
docker-compose up -d postgres redis zookeeper kafka

# Run the app
mvn spring-boot:run
```

App runs at `http://localhost:8080`

---

## API Reference

### Create a rate limit config

```
POST /api/v1/configs
Content-Type: application/json
```

```json
{
  "clientKey": "user-123",
  "maxRequests": 10,
  "windowSeconds": 60,
  "algorithm": "SLIDING_WINDOW"
}
```

`algorithm` accepts: `FIXED_WINDOW` | `SLIDING_WINDOW` | `TOKEN_BUCKET`

One config per clientKey. Use `PUT` to change algorithm or limits.

---

### Check rate limit

Call this on every incoming request to your protected service.

```
POST /api/v1/rate-limit/check
Content-Type: application/json
```

```json
{
  "clientKey": "user-123",
  "endpoint": "/api/orders"
}
```

**200 — allowed:**
```json
{
  "allowed": true,
  "remainingRequests": 7,
  "resetAfterSeconds": 42,
  "clientKey": "user-123"
}
```

**429 — rate limited:**
```json
{
  "allowed": false,
  "remainingRequests": 0,
  "resetAfterSeconds": 15,
  "clientKey": "user-123"
}
```

---

### Get analytics

```
GET /api/v1/analytics/{clientKey}?minutes=60
```

```json
{
  "clientKey": "user-123",
  "totalRequests": 150,
  "allowedRequests": 120,
  "rejectedRequests": 30,
  "rejectionRate": "20.00%",
  "from": "2025-01-01T10:00:00",
  "to": "2025-01-01T11:00:00"
}
```

---

### Update config

```
PUT /api/v1/configs/{clientKey}
Content-Type: application/json

{
  "maxRequests": 20,
  "windowSeconds": 60,
  "algorithm": "TOKEN_BUCKET"
}
```

### Delete config

```
DELETE /api/v1/configs/{clientKey}
```

---

## Testing with Redis CLI

Once the app is running, inspect rate limit state directly:

```bash
docker exec -it rl-redis redis-cli

# Fixed window — see the counter
GET rl:fixed:user-123:<windowTimestamp>
TTL rl:fixed:user-123:<windowTimestamp>

# Sliding window — see individual request timestamps
ZRANGE rl:sliding:user-123 0 -1 WITHSCORES

# Token bucket — see current token count and last refill
HGETALL rl:token:user-123
```

---

## Deployment

The app is deployed on Railway. All services (Spring Boot, Redis, Kafka, PostgreSQL) are containerised and can be spun up locally with a single command:

```bash
docker-compose up -d
```
