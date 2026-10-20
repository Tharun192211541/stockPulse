# StockPulse — AI Inventory & Dynamic Pricing Engine

StockPulse is an inventory management and dynamic pricing application that uses business rules and an optional LLM to generate pricing and replenishment recommendations.

## Project Objective

The central workflow is:

1. Maintain product and inventory information
2. Detect low inventory and demand spikes
3. Publish application events when relevant inventory changes occur
4. Process recommendation generation asynchronously
5. Generate pricing and replenishment recommendations
6. Use an LLM when configured and available
7. Fall back to deterministic business rules when AI fails
8. Persist the generated recommendations
9. Display pending recommendations on a dashboard
10. Require human approval before applying pricing or replenishment changes
11. Update the actual product price or stock only after the relevant recommendation is accepted

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Data JPA
- H2 Database
- Jakarta Bean Validation
- Spring Events and `@Async` for background processing
- Maven for build management

## Domain Model

The application follows a modular monolith architecture with feature-oriented packages:

```
com.stockpulse
├── product
│   ├── Product.java
│   ├── ProductCategory.java
│   ├── ProductStatus.java
│   ├── ProductRepository.java
│   ├── ProductService.java
│   └── ProductController.java
├── suggestion
│   ├── PricingSuggestion.java
│   ├── ReorderSuggestion.java
│   ├── PriceDirection.java
│   ├── SuggestionStatus.java
│   ├── TriggerReason.java
│   ├── PricingSuggestionRepository.java
│   ├── ReorderSuggestionRepository.java
│   ├── SuggestionService.java
│   └── SuggestionController.java
├── commerce
│   ├── CommerceAdvisor.java
│   ├── RuleBasedCommerceAdvisor.java
│   ├── AiCommerceAdvisor.java
│   └── CommerceAdvisorFactory.java
├── event
│   ├── InventoryEvent.java
│   ├── InventoryEventService.java
│   └── InventoryEventListener.java
├── ai
│   └── LLMGateway.java
├── config
│   ├── AsyncConfig.java
│   ├── CorsConfig.java
│   └── DataInitializer.java
└── exception
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── InvalidSuggestionStateException.java
```

## REST API Endpoints

### Products

- `POST /products` — Create a product
- `GET /products` — List products
- `GET /products?status=ACTIVE&category=ELECTRONICS` — Filter products by status and/or category
- `GET /products/{id}` — Retrieve a product
- `PATCH /products/{id}/stock` — Update inventory
- `POST /products/{id}/orders` — Simulate a sale
- `POST /products/{id}/suggest-pricing` — Request a pricing suggestion manually
- `POST /products/{id}/suggest-reorder` — Request a replenishment suggestion manually

### Pricing Suggestions

- `GET /suggestions/pricing` — List pricing suggestions (default: PENDING status)
- `GET /suggestions/pricing?status=ACCEPTED` — List pricing suggestions by status
- `GET /suggestions/pricing/{id}` — Retrieve a pricing suggestion
- `PATCH /suggestions/pricing/{id}` — Accept or reject a pricing suggestion
- `PATCH /pricing-suggestions/{id}` — Alternative endpoint for accepting/rejecting pricing suggestions

### Reorder Suggestions

- `GET /suggestions/reorder` — List reorder suggestions (default: PENDING status)
- `GET /suggestions/reorder?status=ACCEPTED` — List reorder suggestions by status
- `GET /suggestions/reorder/{id}` — Retrieve a reorder suggestion
- `PATCH /suggestions/reorder/{id}` — Accept or reject a reorder suggestion
- `PATCH /reorder-suggestions/{id}` — Alternative endpoint for accepting/rejecting reorder suggestions

### Configuration

- `GET /config/strategy` — Get the current commerce strategy
- `PUT /config/strategy` — Set the commerce strategy (RULE or AI)

## Business Rules

### Rule-based Pricing

- If stock is below the reorder threshold, consider a 10% price increase
- Otherwise, if demand velocity exceeds three times the category-average demand, consider a 5% increase
- Otherwise, recommend HOLD

### Rule-based Replenishment

- Recommended quantity = max(1, reorderThreshold * 3 - currentStock)

## How to Run

1. Make sure you have Java 17+ and Maven installed
2. Navigate to the project directory
3. Run the application:
   ```
   mvn spring-boot:run
   ```
4. Access the application at http://localhost:8080
5. Access the H2 console at http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:testdb
   - Username: sa
   - Password: (empty)

## Sample Data

The application is pre-populated with the exact 8 products specified:

1. SKU-ELEC-001: Wireless Earbuds Pro (ELECTRONICS) - price $79.99, stock 45, threshold 20, demand 3, status ACTIVE
2. SKU-ELEC-002: USB-C Hub 7-Port (ELECTRONICS) - price $34.99, stock 120, threshold 30, demand 1, status ACTIVE
3. SKU-APP-001: Organic Cotton T-Shirt (APPAREL) - price $24.99, stock 8, threshold 15, demand 12, status PRICE_REVIEW_PENDING
4. SKU-APP-002: Running Shorts — Navy (APPAREL) - price $39.99, stock 55, threshold 20, demand 2, status ACTIVE
5. SKU-HOME-001: Ceramic Pour-Over Set (HOME) - price $49.99, stock 22, threshold 10, demand 4, status ACTIVE
6. SKU-HOME-002: LED Desk Lamp — Dimmable (HOME) - price $59.99, stock 0, threshold 15, demand 0, status OUT_OF_STOCK
7. SKU-ELEC-003: Portable Charger 20K (ELECTRONICS) - price $44.99, stock 18, threshold 25, demand 8, status ACTIVE
8. SKU-APP-003: Hoodie — Heather Grey (APPAREL) - price $54.99, stock 11, threshold 12, demand 15, status ACTIVE

## Event Processing

When a sale is processed, the system automatically detects:
- Low inventory situations (stock ≤ reorder threshold)
- Demand spikes (demand velocity > 3 × category average)

These events trigger asynchronous generation of pricing and replenishment suggestions using the configured commerce advisor.

## Human Approval Workflow

All pricing and replenishment suggestions require human approval before being applied:

1. Generated suggestions have PENDING status
2. Users can ACCEPT or REJECT suggestions via the API
3. Accepted pricing suggestions update the product's current price
4. Accepted reorder suggestions increase the product's stock level
5. Product status transitions follow business rules:
   - ACTIVE ↔ PRICE_REVIEW_PENDING ↔ ACTIVE
   - OUT_OF_STOCK when stock reaches zero
   - Return to ACTIVE when stock is replenished (unless other conditions apply)

## Configuration

### Runtime Strategy Switching

The commerce strategy can be changed at runtime:

```bash
# Get current strategy
curl -X GET http://localhost:8080/config/strategy

# Switch to rule-based advisor
curl -X PUT http://localhost:8080/config/strategy \
  -H "Content-Type: application/json" \
  -d '{"strategy":"RULE"}'

# Switch to AI advisor (requires LLM configuration)
curl -X PUT http://localhost:8080/config/strategy \
  -H "Content-Type: application/json" \
  -d '{"strategy":"AI"}'
```

### LLM Configuration

To enable AI-powered recommendations, configure the following environment variables:

```bash
# For Gemini
export LLM_PROVIDER=gemini
export LLM_API_KEY=your-gemini-api-key
export LLM_MODEL=gemini-pro

# For Groq
export LLM_PROVIDER=groq
export LLM_API_KEY=your-groq-api-key
export LLM_MODEL=llama3-70b-8192

# For Ollama (local)
export LLM_PROVIDER=ollama
export LLM_MODEL=llama3
# Ollama runs on localhost:11434 by default
```

### Application Properties

Additional configuration in `application.yaml`:

```yaml
# Demand spike detection multiplier
demand:
  spike:
    multiplier: 3.0

# Spring configuration
spring:
  application:
    name: StockPulse
## Manual Demo Sequence

1. Start the application
2. List all products:
   ```bash
   curl http://localhost:8080/products
   ```
3. Find a product below its reorder threshold (e.g., SKU-APP-001 or SKU-ELEC-003)
4. Manually generate pricing and reorder suggestions:
   ```bash
   curl -X POST http://localhost:8080/products/1/suggest-pricing
   curl -X POST http://localhost:8080/products/1/suggest-reorder
   ```
5. List pending suggestions:
   ```bash
   curl http://localhost:8080/suggestions/pricing
   curl http://localhost:8080/suggestions/reorder
   ```
6. Accept a pricing suggestion:
   ```bash
   curl -X PATCH http://localhost:8080/suggestions/pricing/1 \
     -H "Content-Type: application/json" \
     -d '{"status":"ACCEPTED"}'
   ```
7. Verify the product price has been updated:
   ```bash
   curl http://localhost:8080/products/1
   ```
8. Accept a reorder suggestion:
   ```bash
   curl -X PATCH http://localhost:8080/suggestions/reorder/1 \
     -H "Content-Type: application/json" \
     -d '{"status":"ACCEPTED"}'
   ```
9. Verify the product stock has been updated:
   ```bash
   curl http://localhost:8080/products/1
   ```
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
      
server:
  port: 8080

logging:
  level:
    com.zycus.StockPulse: DEBUG
```
## Configuration

The commerce strategy can be configured in `application.yaml`:

```yaml
commerce:
  strategy: RULE  # or AI when implemented
```

## Testing

Run the tests with:

```
mvn test
```

## Future Enhancements

- Integration with AI/LLM providers for smarter recommendations
- Angular-based dashboard for UI management
- Enhanced analytics and reporting
- Multi-user support with role-based access control