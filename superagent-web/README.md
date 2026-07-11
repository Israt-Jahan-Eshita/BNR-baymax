# SuperAgent Web — Frontend

This is the Next.js frontend for bKash Baymax.

## Setup Instructions (For Teammate)

```bash
cd superagent-web
npx create-next-app@latest ./ --typescript --tailwind --eslint --app --src-dir --use-npm
npm run dev
```

## Backend API Base URL
The Spring Boot backend runs at `http://localhost:8080`.

## Key API Endpoints to Connect

### 1. Dashboard (GET)
```
GET http://localhost:8080/api/dashboard/{agentId}
```
Returns: physical cash, provider balances (bKash, Nagad, Rocket), recent transactions, active alerts.

### 2. Simulate Transaction (POST)
```
POST http://localhost:8080/api/simulate
Content-Type: application/json

{
  "agentId": 1,
  "providerName": "bKash",
  "type": "cash_out",
  "amount": 20000,
  "counterpartyAccount": "01712345678"
}
```

### 3. Alert Management (PATCH)
```
PATCH http://localhost:8080/api/alerts/{alertId}/status
Content-Type: application/json

{
  "status": "acknowledged",
  "assignedTo": "field_officer",
  "notes": "Checking with agent"
}
```

## Pages to Build

1. **`/` — Agent Dashboard**: Shows physical cash gauge, 3 provider balance cards, liquidity warning banner, recent transactions list.
2. **`/alerts` — Operations Inbox**: List of AI-generated alerts with Acknowledge/Escalate/Resolve buttons.
3. **`/simulate` — Simulator Panel**: Form to inject test transactions for the live demo.
