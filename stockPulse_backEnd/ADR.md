# ADR-001: Pluggable commerce advice and asynchronous recommendation loop

- **Status:** Accepted for the StockPulse demo backend
- **Date:** 2026-10-20

## Context

StockPulse needs to turn inventory changes into pricing and replenishment suggestions, support deterministic behavior when an LLM is unavailable, and let a merchandiser approve or reject recommendations. The same advice contract must serve manual HTTP requests and automatic event handling. Recommendation generation must not extend the stock/order request latency.

## Decision

### 1. One `CommerceAdvisor` contract

`CommerceAdvisor` returns both a `PricingSuggestion` and a `ReorderSuggestion`. `RuleBasedCommerceAdvisor` provides a deterministic baseline; `AiCommerceAdvisor` calls an OpenAI-compatible gateway and validates structured output. `CommerceAdvisorFactory` selects the active implementation from `CommerceStrategyHolder`, an atomic runtime setting (`RULE` or `AI`). This avoids duplicated caller logic and makes future advisors register behind the same contract.

### 2. Validate AI output and fail safely

AI output is parsed as JSON. Recommended prices must be positive and within 0.5×–2× the current price, the direction must agree with the actual price delta, confidence must be in [0, 1], and reorder quantity must be a positive bounded integer. Gateway timeouts, malformed responses, or invalid output use the rule-based implementation instead of silently dropping the recommendation. The API key is supplied through environment configuration, never source control.

### 3. Publish inventory events after transaction commit

Stock updates and simulated sales persist first. `InventoryEventService` publishes an event and `InventoryEventListener` processes it asynchronously after commit. Both pricing and reorder suggestions are generated in that path. This keeps the write API independent from LLM latency and avoids a listener reading uncommitted product state.

### 4. Human approval is the state transition boundary

Suggestions start as `PENDING` and may transition only to `ACCEPTED` or `REJECTED`. Acceptance has a domain side effect: pricing acceptance updates product price, while reorder acceptance increments stock. Product rows are locked when creating deduplicated suggestions or applying an accepted action; suggestion rows carry optimistic version fields so concurrent transitions cannot both commit successfully. Pending duplicate detection is scoped to product + trigger reason + suggestion type.

### 5. Storage and extension points

The demo uses H2 in-memory storage and `create-drop` for a frictionless run. It is not a production persistence choice. `costPrice` is retained as an optional product field to support future margin floors; a future migration can add competitor prices and supplier/catalog identifiers without changing the advisor contract.

## Consequences

- Strategy can be changed without restarting the application.
- Rule-only operation is available without LLM credentials.
- Manual and automatic flows share suggestion creation and deduplication logic.
- The current demo's demand velocity is a counter, not a true rolling 24-hour metric.
- A production deployment should use a persistent database, schema migrations, authentication, retry/dead-letter policy for event failures, observability, and integration tests against its chosen database.

## Demo workflow

1. Create a product with `POST /products`.
2. Simulate a sale with `POST /products/{id}/orders` or update stock with `PATCH /products/{id}/stock`.
3. Poll `GET /suggestions/pricing?status=PENDING` and `GET /suggestions/reorder?status=PENDING`.
4. Accept a pricing suggestion using `PATCH /pricing-suggestions/{id}` and verify `currentPrice` changed.
5. Accept a reorder suggestion using `PATCH /reorder-suggestions/{id}` and verify stock increased.
6. Change `/config/strategy` from `RULE` to `AI` to demonstrate runtime selection; omit AI configuration to demonstrate the fallback path.
