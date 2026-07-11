# BNR Baymax Deployment Guide

This guide covers deploying the backend to Render, the frontend to Render, and the database to Supabase.

## 1. Supabase (Database)
1. Create a Supabase project.
2. Note down the database password securely.
3. Open the project's **Connect** panel. Select **Session Pooler** (port 5432).
4. Note the host, username, and database name.
5. Construct the Spring JDBC URL:
   `jdbc:postgresql://<SUPABASE_SESSION_POOLER_HOST>:5432/postgres?sslmode=require`

## 2. Render Backend
1. Create a Render Web Service.
2. Select your GitHub Repository and `main` branch.
3. **Root Directory**: `superagent-api`
4. **Runtime**: `Docker`
5. Set Environment variables:
   - `DATABASE_URL`: The Supabase JDBC string.
   - `DATABASE_USERNAME`: Your Supabase user.
   - `DATABASE_PASSWORD`: Your Supabase password.
   - `OPENAI_API_KEY`: Your OpenAI key.
   - `ALLOWED_ORIGINS`: (Leave blank for now, will update after frontend deployment).
6. Enable **Auto-Deploy: After CI Checks Pass**.

## 3. Render Frontend
1. Create a second Render Web Service.
2. **Root Directory**: `superagent-web`
3. **Runtime**: `Node`
4. **Build Command**: `npm ci && npm run build`
5. **Start Command**: `npm run start`
6. Set Environment variable:
   - `NEXT_PUBLIC_API_BASE_URL`: The HTTPS URL of your newly deployed Render backend.
     *(IMPORTANT: This must be set BEFORE the frontend builds, as NEXT_PUBLIC variables are baked into the build).*
7. Enable **Auto-Deploy: After CI Checks Pass**.

## 4. Final Wiring
1. Copy the URL of your new Render frontend.
2. Go back to your Render Backend's environment variables.
3. Set `ALLOWED_ORIGINS` to the exact frontend URL (e.g., `https://bnr-baymax-web.onrender.com`).
4. Redeploy the Backend.

---

## Production Verification Checklist

### BACKEND:
- [ ] Render service starts successfully
- [ ] `/api/health` returns HTTP 200
- [ ] Application connects to Supabase through JDBC
- [ ] No local PostgreSQL dependency is required
- [ ] No secret is shown in logs
- [ ] Dashboard API responds
- [ ] Alerts API responds
- [ ] Cases API responds
- [ ] Scenarios API responds
- [ ] Validation API responds
- [ ] AI endpoint fails safely if the AI provider has an external error

### FRONTEND:
- [ ] Next.js homepage loads
- [ ] Dashboard loads real backend data
- [ ] No browser request goes to `localhost:8080`
- [ ] No CORS error appears
- [ ] Alerts page loads
- [ ] Cases page loads
- [ ] Scenario Lab runs a scenario
- [ ] Dashboard refresh reflects backend changes
- [ ] Validation metrics page loads
- [ ] Baymax AI uses the deployed backend

### DATABASE:
- [ ] Expected tables exist in Supabase
- [ ] Synthetic/demo data initializes according to current project behavior
- [ ] Provider balances remain separate
- [ ] Existing synthetic transaction and alert behavior still works

### QUALITY:
- [ ] Docker SonarQube opens at `localhost:9000`
- [ ] `bnr-baymax-api` analysis exists
- [ ] `bnr-baymax-web` analysis exists
- [ ] Analysis projectVersion identifies the Git commit SHA
- [ ] Backend Quality Gate passes
- [ ] Frontend Quality Gate passes
- [ ] An unscanned commit is blocked by pre-push
- [ ] A commit without `Prompt:` metadata is blocked by pre-push
- [ ] GitHub Actions backend job passes
- [ ] GitHub Actions frontend job passes
- [ ] Render only deploys after CI checks pass
