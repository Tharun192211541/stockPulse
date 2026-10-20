# StockPulse backend

StockPulse is a Spring Boot 3 / Java 17 backend for an inventory-signal → pricing/replenishment recommendation → human approval workflow. It keeps AI advice behind the same `CommerceAdvisor` contract as the deterministic rule implementation.

## Requirements

- JDK 17
- Maven 3.8+ (or use `./mvnw` where the wrapper distribution is available)
- An OpenAI-compatible LLM endpoint is optional; the rule strategy works without it.

## Run locally

```bash
# from the repository root
./mvnw spring-boot:run
```

If the wrapper has no cached Maven distribution, install Maven 3.8+ and run `mvn spring-boot:run` instead. The current default profile uses an in-memory H2 database (`jdbc:h2:mem:testdb`) and `ddl-auto: create-drop`, so data is reset when the application restarts. This is for local/demo use, not production persistence.

### Configure AI (optional)

```bash
export LLM_BASE_URL="https://your-openai-compatible-host/v1"
export LLM_API_KEY="your-key"
export LLM_MODEL="your-model-name"
export LLM_PRODUCT_HEADER="PC1" # optional gateway-specific routing header
export DEMAND_SPIKE_MULTIPLIER="3.0" # configure demand.spike.multiplier via application environment as needed
./mvnw spring-boot:run
```

`LLM_API_KEY` is intentionally not committed. The AI advisor uses bounded request timeouts and falls back to `RuleBasedCommerceAdvisor` on gateway errors, invalid JSON, out-of-range values, or inconsistent price direction. The default strategy is `RULE`.

## Core API

All examples assume `http://localhost:8080`.

### Create a product

```bash
curl -X POST http://localhost:8080/products \
  -H 'Content-Type: application/json' \
  -d '{
    "sku":"KB-100",
    "name":"Mechanical Keyboard",
    "category":"ELECTRONICS",
    "currentPrice":99.99,
    "costPrice":45.00,
    "stockLevel":8,
    "reorderThreshold":10,
    "demandVelocity":4
  }'
```

Required fields: `sku`, `name`, `category`, and positive `currentPrice`. Stock, reorder threshold, and demand velocity must be non-negative. `costPrice` is optional and non-negative.

### List/filter products

```bash
curl 'http://localhost:8080/products?status=ACTIVE&category=ELECTRONICS'
```

### Simulate a sale or adjust stock

```bash
curl -X POST http://localhost:8080/products/1/orders \
  -H 'Content-Type: application/json' -d '{"quantity":2}'

curl -X PATCH http://localhost:8080/products/1/stock \
  -H 'Content-Type: application/json' -d '{"stockLevel":3}'
```

The request persists the stock change and returns without waiting for recommendation generation. After transaction commit, the event listener asynchronously generates pricing and reorder suggestions. Duplicate pending suggestions are skipped for the same product, trigger reason, and suggestion type. AI failures fall back to rules.

### Runtime strategy selection

```bash
curl http://localhost:8080/config/strategy
curl -X PUT http://localhost:8080/config/strategy \
  -H 'Content-Type: application/json' -d '{"strategy":"AI"}'
# switch back to deterministic mode
curl -X PUT http://localhost:8080/config/strategy \
  -H 'Content-Type: application/json' -d '{"strategy":"RULE"}'
```

Accepted values are `RULE` and `AI` (case-insensitive). Switching is runtime-only and does not restart the application.

### Manual recommendations

```bash
curl -X POST http://localhost:8080/products/1/suggest-pricing
curl -X POST http://localhost:8080/products/1/suggest-reorder
```

Repeated requests return an existing pending manual suggestion rather than adding another one.

### Review recommendations

```bash
curl 'http://localhost:8080/suggestions/pricing?status=PENDING'
curl 'http://localhost:8080/suggestions/reorder?status=PENDING'

curl -X PATCH http://localhost:8080/pricing-suggestions/1 \
  -H 'Content-Type: application/json' -d '{"status":"ACCEPTED"}'

curl -X PATCH http://localhost:8080/reorder-suggestions/1 \
  -H 'Content-Type: application/json' -d '{"status":"REJECTED"}'
```

Accepting a pricing suggestion updates `currentPrice`; accepting a reorder suggestion increments stock by the recommended quantity. A suggestion can transition only from `PENDING` to `ACCEPTED` or `REJECTED`. Repeated or invalid transitions return a conflict/bad-request response.

## Tests

```bash
./mvnw test
```

Tests cover strategy switching, AI response validation and rule fallback, event listener creation of both recommendation types, duplicate pending suggestion handling, and acceptance/state transitions. Run the command in an environment with Maven and dependencies available; the project should not be described as verified until this command completes successfully.

## Current limitations

- H2 in-memory storage is intentionally demo-only; use a persistent database and migrations for deployment.
- Demand velocity is a simple counter for the demo rather than a rolling 24-hour event aggregation.
- The event listener logs persistence/domain failures; production deployments should add retries/dead-letter handling and metrics.
- Authentication/authorization and rate limiting are not configured in this sample.
