# bKash Baymax — SuperAgent Liquidity & Risk Intelligence Platform

> bKash presents SUST CSE Carnival 2026 — Codex Community Hackathon

## Overview

bKash Baymax is an AI-powered decision-support platform for multi-provider mobile financial service (MFS) agents. It provides a unified view of physical cash and separate e-money balances across bKash, Nagad, and Rocket, predicts liquidity shortages, detects unusual transaction patterns, and coordinates operational responses through a case management system.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   SUPERAGENT WEB                    │
│            (Next.js + TailwindCSS)                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Dashboard │  │  Alerts  │  │  Simulator Panel │  │
│  │  (Agent)  │  │  (Ops)   │  │    (Demo)        │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└────────────────────┬────────────────────────────────┘
                     │ REST API
┌────────────────────┴────────────────────────────────┐
│                  SUPERAGENT API                     │
│             (Spring Boot + Java 21)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Dashboard │  │  Alert   │  │   Transaction    │  │
│  │ Controller│  │Controller│  │   Simulator      │  │
│  └─────┬────┘  └────┬─────┘  └───────┬──────────┘  │
│        │             │                │              │
│  ┌─────┴─────────────┴────────────────┴──────────┐  │
│  │           RiskAnalyzerService (AI Brain)       │  │
│  │  - Liquidity Prediction                       │  │
│  │  - Anomaly Detection                          │  │
│  │  - Banglish Alert Generation                  │  │
│  └───────────────────┬───────────────────────────┘  │
│                      │ OpenAI / Groq API             │
└──────────────────────┼──────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────┐
│              PostgreSQL (Docker)                     │
│  agents | provider_balances | transactions | alerts  │
└─────────────────────────────────────────────────────┘
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Backend | Java 21, Spring Boot 4.1 |
| Frontend | Next.js, React, TailwindCSS |
| AI Engine | OpenAI GPT-4o / Groq LLaMA 3.3 70B |
| Database | PostgreSQL 16 |
| Deployment | Docker, Render |

## Project Structure

```
bkash-baymax/
├── superagent-api/     # Spring Boot backend
│   ├── src/main/java/com/bkash/baymax/
│   │   ├── controller/     # REST API endpoints
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access
│   │   ├── service/        # Business logic + AI
│   │   └── dto/            # Request/Response objects
│   └── pom.xml
├── superagent-web/     # Next.js frontend
│   └── src/app/
│       ├── page.tsx        # Agent Dashboard
│       ├── alerts/         # Operations Inbox
│       └── simulate/       # Demo Simulator
└── README.md
```

## Setup

### Backend
```bash
cd superagent-api
./mvnw.cmd spring-boot:run
```

### Frontend
```bash
cd superagent-web
npm install
npm run dev
```

### Environment Variables
```
OPENAI_API_KEY=your-key-here
DATABASE_URL=jdbc:postgresql://localhost:5432/baymax
```

## Team
- Built for SUST CSE Carnival 2026 Hackathon
- Powered by AI (OpenAI GPT-4o / Groq LLaMA 3.3)

## Responsible Design
- All data is synthetic/simulated — no real customer data
- Anomalies are flagged as "requires review", never as confirmed fraud
- Human review is required before any action
- Provider boundaries are strictly maintained
