# Setup & Run Instructions

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java JDK | 21+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org or bundled with IDE |
| Node.js | 20+ | https://nodejs.org |
| Git | any | https://git-scm.com |

---

## Step 1 — Get Your API Keys

### Anthropic (Claude AI) — Required
1. Go to https://console.anthropic.com
2. Create an account and generate an API key
3. Pricing: ~$0.003 per 1K input tokens (claude-sonnet-4-6). A full 5-phase analysis uses ~50-100K tokens = ~$0.15-0.30 per run.

### Search Provider — Optional but recommended for live data
Without a search key, the app runs in mock mode (Claude still analyzes, but with placeholder search data).

**Tavily (recommended):**
1. Go to https://tavily.com
2. Sign up for free tier (1000 searches/month free)
3. Copy your API key

**Brave Search (alternative):**
1. Go to https://api.search.brave.com
2. Sign up for free tier (2000 searches/month)
3. Copy your API key

---

## Step 2 — Configure Environment

```bash
cd backend
cp .env.example .env
```

Edit `.env`:
```
ANTHROPIC_API_KEY=sk-ant-...your-key-here...
TAVILY_API_KEY=tvly-...your-key-here...   # optional
```

If using Tavily, also update `src/main/resources/application.properties`:
```properties
search.provider=tavily
```

---

## Step 3 — Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Or on Windows:
```cmd
cd backend
mvnw.cmd spring-boot:run
```

Backend starts at: http://localhost:8080

Verify it's running:
```
GET http://localhost:8080/actuator/health
→ {"status":"UP"}
```

---

## Step 4 — Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at: http://localhost:5173

---

## Step 5 — Use the App

1. Open http://localhost:5173
2. Fill in your investor profile
3. Click "Run Investment Analysis"
4. Wait 1-3 minutes for all 5 phases to complete
5. View the report by phase — use the tabs at the top

---

## API Reference (for developers)

```
POST /api/research/analyze        Run a full analysis
GET  /api/research/reports        List all saved reports
GET  /api/research/reports/{id}   Get a specific report
DELETE /api/research/reports/{id} Delete a report
```

Example request body for `/api/research/analyze`:
```json
{
  "country": "Canada",
  "investmentAmount": 25000,
  "riskTolerance": "MODERATE",
  "timelineYears": 7,
  "sectorInterests": ["Technology", "Energy", "Financials"],
  "goal": "GROWTH",
  "accounts": ["TFSA", "RRSP"],
  "currentHoldings": ["SHOP.TO", "ENB.TO"]
}
```

---

## Switching Search Providers

In `application.properties`, change `search.provider` to:
- `mock` — no API key needed, returns placeholder data
- `tavily` — requires `TAVILY_API_KEY` in `.env`
- `brave` — requires `BRAVE_API_KEY` in `.env` (implement `BraveSearchService.java` following TavilySearchService pattern)

---

## Common Issues

| Problem | Fix |
|---------|-----|
| `401 Unauthorized` from Claude | Check `ANTHROPIC_API_KEY` in `.env` |
| Analysis takes too long / times out | Normal for 5-phase analysis. Frontend timeout is 5 minutes. |
| Port 8080 already in use | Change `server.port=8081` in `application.properties` |
| CORS error in browser | Check `WebConfig.java` — add your frontend origin |
| Spring AI dependency not found | Add Spring milestone repository to `pom.xml` (already included) |
