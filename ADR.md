# Architectural Decision Records (ADRs)

## 1. Modular Monolith Architecture

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to choose between a monolithic and microservices architecture for the StockPulse application.

### Decision
We chose a modular monolith architecture with feature-oriented packages.

### Consequences
- **Positive**: Simpler deployment, easier development and debugging, reduced network latency
- **Negative**: Less scalability for independent service scaling, tighter coupling between features

## 2. H2 In-Memory Database

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed a database solution for rapid development and testing.

### Decision
We chose H2 in-memory database for development purposes.

### Consequences
- **Positive**: Zero setup, fast performance, easy testing
- **Negative**: Data is lost on application restart, not suitable for production

## 3. Separate Entities for Pricing and Reorder Suggestions

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to decide whether to use a single suggestion entity or separate entities for pricing and reorder suggestions.

### Decision
We chose separate entities for pricing and reorder suggestions.

### Consequences
- **Positive**: Clear separation of concerns, independent validation rules, easier maintenance
- **Negative**: More entities to manage, potential duplication of common fields

## 4. Asynchronous Recommendation Generation

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to decide how to handle recommendation generation triggered by inventory events.

### Decision
We chose to generate recommendations asynchronously using Spring Events and @Async.

### Consequences
- **Positive**: Non-blocking sales processing, better user experience, scalable processing
- **Negative**: Increased complexity, potential for event loss (in-memory events), eventual consistency

## 5. Deterministic Fallback Strategy

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to ensure the system works even when AI services are unavailable.

### Decision
We implemented deterministic business rules as a fallback when AI integration fails or is not configured.

### Consequences
- **Positive**: System always functional, predictable behavior, easier testing
- **Negative**: Less sophisticated recommendations compared to AI

## 6. Mandatory Human Approval

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to decide whether recommendations should be applied automatically or require human approval.

### Decision
We made human approval mandatory for all pricing and replenishment suggestions.

### Consequences
- **Positive**: Better control, reduced risk of incorrect decisions, compliance-friendly
- **Negative**: Slower response time, requires user intervention

## 7. In-Memory Event Limitations

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We chose Spring's in-memory event system for simplicity but acknowledged its limitations.

### Decision
We accepted the limitations of in-memory events for this implementation.

### Consequences
- **Positive**: Simple implementation, no external dependencies
- **Negative**: Events lost on application restart, not durable, no retry mechanism

## 8. Duplicate Prevention Strategy

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to prevent multiple identical pending suggestions for the same product and trigger.

### Decision
We implemented a simple check in the event listener to prevent duplicate pending suggestions.

### Consequences
- **Positive**: Prevents unnecessary processing, reduces database clutter
- **Negative**: Race conditions possible under high load, requires querying suggestions in event handler
## 9. Unified Commerce Advisor Contract

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to decide between a unified CommerceAdvisor interface or separate interfaces for pricing and reorder recommendations.

### Decision
We chose a unified CommerceAdvisor interface that handles both pricing and reorder suggestions. This allows for consistent strategy switching and shared context between related recommendations.

### Consequences
- **Positive**: Single point of strategy control, consistent API, easier implementation switching
- **Negative**: Tighter coupling between pricing and reorder logic, less flexibility for independent evolution

## 10. Manual-Only Suggestion Generation

**Date**: October 20, 2026  
**Status**: Accepted

### Context
The original design included automatic suggestion generation triggered by inventory events. However, business requirements emphasized manual control.

### Decision
We removed all automatic suggestion generation and made suggestion creation explicitly manual through dedicated API endpoints. Events are still published for other purposes but do not trigger suggestions.

### Consequences
- **Positive**: Explicit control over recommendation generation, prevents unwanted suggestions, clearer audit trail
- **Negative**: Requires more user interaction, potential for missed opportunities

## 11. Runtime Strategy Switching

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed a way to switch between rule-based and AI-based strategies without restarting the application.

### Decision
We implemented a thread-safe CommerceStrategyHolder that allows runtime switching via API endpoints. The factory reads the current strategy on each invocation.

### Consequences
- **Positive**: No downtime for strategy changes, immediate effect, easy testing of different approaches
- **Negative**: Slight performance overhead from strategy lookup, potential for inconsistent behavior during concurrent requests

## 12. LLM Failure Handling and Fallback

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to ensure the system remains functional and secure when LLM providers are unavailable or return invalid responses.

### Decision
We implemented comprehensive error handling with structured validation of AI responses. Failed AI calls automatically fall back to rule-based advisors with clear indication in the reasoning.

### Consequences
- **Positive**: System always functional, transparent failure handling, protects against invalid AI outputs
- **Negative**: More complex error handling code, potential for masking AI issues

## 13. Product Lifecycle Management

**Date**: October 20, 2026  
**Status**: Accepted

### Context
We needed to manage product status transitions correctly, especially around pricing reviews and out-of-stock conditions.

### Decision
We implemented precise lifecycle rules: ACTIVE ↔ PRICE_REVIEW_PENDING ↔ ACTIVE transitions for pricing suggestions, and OUT_OF_STOCK status when stock reaches zero with proper restoration when restocked.

### Consequences
- **Positive**: Correct business state representation, prevents invalid operations
- **Negative**: Complex state management logic, careful coordination required between suggestion acceptance and product updates