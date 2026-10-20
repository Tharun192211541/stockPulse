# StockPulse Angular Frontend

A standalone Angular frontend for the StockPulse Spring Boot REST API. It includes product catalog management, inventory updates, simulated sales, pricing and reorder recommendations, suggestion approval/rejection, filtering, and commerce strategy switching.

## Requirements

- Node.js 18.19+ or 20+
- npm
- StockPulse Spring Boot backend running at `http://localhost:8080`

## Run it

```bash
npm install
npm start
```

Open `http://localhost:4200`.

For a production build:

```bash
npm run build
```

## API configuration

The base URL is configured in `src/app/api.service.ts`:

```ts
readonly baseUrl = 'http://localhost:8080';
```

Change it if your backend runs elsewhere. The backend CORS configuration in the provided project already permits `http://localhost:4200`.

## Endpoints used

| Method | Endpoint | UI action |
|---|---|---|
| GET | `/products` | Catalog and dashboard |
| GET | `/products?status=&category=` | Catalog filters |
| GET | `/products/{id}` | Product details |
| POST | `/products` | Create product |
| PATCH | `/products/{id}/stock` | Set stock level |
| POST | `/products/{id}/orders` | Simulate sale |
| POST | `/products/{id}/suggest-pricing` | Generate pricing suggestion |
| POST | `/products/{id}/suggest-reorder` | Generate reorder suggestion |
| GET | `/suggestions/pricing?status=` | Pricing suggestions and status filter |
| GET | `/suggestions/pricing/{id}` | Pricing suggestion details endpoint (available in API service) |
| PATCH | `/suggestions/pricing/{id}` | Accept/reject pricing suggestion |
| GET | `/suggestions/reorder?status=` | Reorder suggestions and status filter |
| GET | `/suggestions/reorder/{id}` | Reorder suggestion details endpoint (available in API service) |
| PATCH | `/suggestions/reorder/{id}` | Accept/reject reorder suggestion |
| GET | `/config/strategy` | Read active advisor strategy |
| PUT | `/config/strategy` | Switch between `RULE` and `AI` |

The backend also defines compatibility PATCH routes under `/suggestions/pricing-suggestions/{id}` and `/suggestions/reorder-suggestions/{id}`. This frontend uses the primary `/suggestions/pricing/{id}` and `/suggestions/reorder/{id}` routes.

## Notes

- Accepting a pricing suggestion updates the product's current price in the backend.
- Accepting a reorder suggestion increments product stock in the backend.
- Only `PENDING` suggestions can be accepted or rejected. The interface disables decision buttons for completed suggestions.
- Stock and sales operations can trigger asynchronous recommendation generation depending on backend rules. Refresh suggestions if they do not appear immediately.
- The backend uses an in-memory H2 database in the supplied configuration, so data may reset when the backend restarts.
- This is a frontend project; the Spring Boot backend is not duplicated in this archive.
