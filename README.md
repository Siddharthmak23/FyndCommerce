# Fynd Commerce Order Console

A responsive two-page commerce order console for creating orders and managing fulfilment.

## Technology stack

- React, TypeScript, Vite
- Axios
- Spring Boot 3, Java 17
- Spring Data JPA, Spring Validation
- H2 file database

## Features

- Catalogue and cart with live stock
- Transactional multi-product order placement
- Order search and status filtering
- Order details and fulfilment actions
- Atomic cancellation with stock restoration

## Prerequisites

- Java 17+
- Maven
- Node.js 18+

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Seed data

The H2 database seeds one catalogue row for each of the six specified products only when the product table is empty. Data persists in `backend/data/orderdb`.

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/products` | List catalogue |
| POST | `/api/orders` | Create order |
| GET | `/api/orders` | List/search/filter orders |
| GET | `/api/orders/{id}` | Get order details |
| PATCH | `/api/orders/{id}/status` | Change status or cancel |

## Order lifecycle

`PLACED -> PACKED -> SHIPPED`

`PLACED` or `PACKED` orders may be cancelled. Repeated, skipped, and invalid transitions are rejected.

## Inventory consistency

Order placement and cancellation are transactional. Requested products are loaded in a consistent ID order with pessimistic locks; stock is checked before any deduction and the order plus items are saved together. Cancellation locks the order, restores quantities only once, and changes status atomically.
