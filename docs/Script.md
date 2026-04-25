# Investment Research Workflow Script

This document defines the research workflow implemented across the 5 AI phases.
It mirrors the original prompt structure and serves as the source of truth for what each phase does.

---

## Investor Profile Schema

```json
{
  "country": "Canada",
  "investmentAmount": 25000,
  "riskTolerance": "MODERATE",          // CONSERVATIVE | MODERATE | AGGRESSIVE
  "timelineYears": 7,
  "sectorInterests": ["Technology", "Energy", "Financials"],
  "goal": "GROWTH",                      // GROWTH | INCOME | PRESERVATION | SPECULATIVE
  "accounts": ["TFSA", "RRSP"],          // TFSA | RRSP | NON_REGISTERED
  "currentHoldings": ["SHOP.TO", "ENB.TO"]
}
```

---

## Phase 1 — Macro Environment

**Goal:** Establish the macroeconomic backdrop.

**Searches:**
1. Bank of Canada interest rate decision (latest) and forward guidance
2. Fed interest rate stance and Canadian market impact
3. CAD/USD exchange rate trend
4. Canadian CPI / inflation data (latest month)
5. TSX composite — current level, 30-day trend, 52-week context
6. S&P 500 — current level and trend for comparison
7. Major geopolitical/trade events affecting Canadian markets (tariffs, trade agreements, commodity shocks)

**Output:** "Market Weather Report" — is the environment risk-on or risk-off? Tailwinds and headwinds.

---

## Phase 2 — Sector Pulse

**Goal:** Score each investor-selected sector for current momentum and near-term outlook.

**Searches per sector:**
1. `[sector] stocks Canada TSX outlook 2026`
2. `[sector] ETF performance 2026`
3. `[sector] catalysts risks next 6 months 2026`

**Output:** Sector scorecard table:
| Sector | Momentum | Key Catalyst | Key Risk | In/Out of Favor |

---

## Phase 3 — Smart Money Tracking

**Goal:** Identify what institutional investors are buying and selling.

**Searches:**
1. 13F filings — Berkshire Hathaway, Bridgewater, Renaissance, Citadel, Pershing Square
2. Canadian institutional investors / CPP Investments
3. Brookfield Asset Management recent acquisitions
4. Warren Buffett latest stock purchases 2026
5. Bill Ackman portfolio changes 2026
6. Michael Burry 13F latest 2026
7. Cathie Wood ARK Invest trades 2026
8. Prem Watsa Fairfax Financial holdings 2026
9. Top TSX insider buying this month
10. Canadian hedge fund top picks 2026

**Output:** Smart Money Signal — convergent themes, contrarian bets, Canadian-specific moves, overlap with investor sectors.

*Note: 13F filings have ~45-day delay — always flagged in output.*

---

## Phase 4 — Stock Screening

**Goal:** Identify and deeply analyze 3-5 stock picks that match the investor profile.

**Process:**
1. Generate candidate list (8-12 stocks) based on Phases 1-3 context
2. Filter to 3-5 best fits based on risk tolerance, timeline, goal, sectors

**Output per stock:**
```
STOCK: [Ticker] — [Company Name]
Exchange: [TSX / NYSE / NASDAQ]
Current Price: [value] (as of [date])
Sector: [sector]

WHY IT FITS YOUR PROFILE: [1-2 sentences]
SMART MONEY ACTIVITY: [institutional holders / insider activity]
RECENT NEWS & CATALYSTS: [2-3 items, last 30 days]

KEY METRICS:
- P/E Ratio: [value] — [high/low vs sector]
- Revenue Growth (YoY): [%]
- Dividend Yield: [%]
- Market Cap: [value]
- Analyst Consensus: [Buy/Hold/Sell] — avg target: [price]

TECHNICAL:
- 52-Week Range: [low — high]
- Current position: [near low / mid / near high]
- 3-month trend: [up/down/sideways]

RISK FACTORS:
1. [Specific risk]
2. [Valuation risk if P/E > 40]
3. [Sector or macro risk]

STOCK TYPE: [Growth / Value / Dividend / GARP / Speculative / Turnaround]
TAX ACCOUNT: [TFSA / RRSP / Non-registered — with reason]
```

---

## Phase 5 — Portfolio Construction

**Goal:** Synthesize picks into a coherent portfolio brief.

**No new searches** — uses Phase 4 output + macro context.

**Output sections:**
1. **How these picks work together** — diversification, sector balance, growth vs income
2. **Position sizing** — equal-weight vs conviction-weighted, with dollar examples
3. **Watchlist triggers** — specific events/prices that would change each thesis
4. **Portfolio scorecard** — summary table
5. **Disclaimer** — "This is educational research, not financial advice."

---

## Rules (enforced in all phases)

- Frame as educator, not advisor: "here is what the data shows" not "you should buy"
- Be honest about valuation risk (flag if P/E > 40)
- Prefer TSX-listed stocks over US-listed when quality is equal
- Distinguish facts (earnings, prices) from opinions (analyst targets, forecasts)
- Always note date of information
- If data is unavailable or mock, say so explicitly
- End every report with the disclaimer

---

## Weekly Check-In (future feature)

Run a quick update on existing holdings:
1. News in last 7 days per ticker
2. Price movement vs last analysis levels
3. Upcoming earnings/ex-dividend dates
4. Smart money changes (insider selling, institutional filing updates)
5. Flag any "ACTION NEEDED" items prominently

---

## Deep Dive (future feature)

Single-stock deep research:
1. Last 4 quarterly earnings — revenue/EPS trend
2. Analyst reports and price targets — bull case vs bear case
3. Institutional ownership changes (last 2 quarters)
4. Insider buying/selling (last 6 months)
5. Competitive landscape
6. Pending litigation, regulatory changes, catalysts
7. Bear case research — "why this stock could fail"
