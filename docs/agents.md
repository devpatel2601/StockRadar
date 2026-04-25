# AI Agents — Architecture & Customization

## Overview

The research workflow is split into 5 sequential agents (phases). Each agent:
1. Generates a set of search queries relevant to its role
2. Executes those queries via the `SearchService`
3. Sends the results to Claude with a phase-specific prompt
4. Returns a structured `PhaseResult` containing the synthesis

The `ResearchOrchestrator` chains them in order, passing all previous phase results as context to each subsequent phase.

---

## Phase Agents

### Phase 1 — MacroEnvironmentPhase
**File:** `service/research/phases/MacroEnvironmentPhase.java`

**Role:** Establishes the macroeconomic backdrop before any stock analysis.

**Searches (7 queries):**
- Bank of Canada rate decisions
- Fed rate stance and Canadian market impact
- CAD/USD exchange rate
- Canadian CPI/inflation
- TSX composite trend
- S&P 500 comparison
- Geopolitical/trade events

**Output:** "Market Weather Report" — risk-on vs risk-off environment, tailwinds/headwinds.

---

### Phase 2 — SectorPulsePhase
**File:** `service/research/phases/SectorPulsePhase.java`

**Role:** Evaluates each of the investor's chosen sectors for current momentum.

**Searches:** 3 queries per sector (outlook, ETF performance, catalysts/risks)

**Output:** Sector scorecard table with momentum rating, key catalyst, key risk, in/out-of-favor assessment.

**Customization:** Sectors come from `InvestorProfile.sectorInterests`. Defaults to Tech/Energy/Financials if none specified.

---

### Phase 3 — SmartMoneyPhase
**File:** `service/research/phases/SmartMoneyPhase.java`

**Role:** Tracks what institutional investors and well-known fund managers are doing.

**Searches (10 queries):**
- 13F filings: Berkshire, Bridgewater, Citadel
- Warren Buffett, Bill Ackman, Michael Burry, Cathie Wood
- CPP Investments, Brookfield, Prem Watsa/Fairfax
- TSX insider buying, Canadian hedge funds

**Output:** Convergent themes, contrarian bets, Canadian-specific moves, overlap with investor's sectors.

**Important note:** 13F filings have a ~45-day delay. The agent flags this in every synthesis.

---

### Phase 4 — StockScreeningPhase
**File:** `service/research/phases/StockScreeningPhase.java`

**Role:** Identifies and deep-dives 3-5 specific stock picks matching the investor profile.

**Searches:** 2-3 queries per sector + 2 general TSX queries

**Context used:** Macro summary (Phase 1), Sector summary (Phase 2), Smart money signals (Phase 3)

**Output per stock:**
- Ticker, exchange, current price
- Why it fits this specific profile
- Smart money activity on the stock
- Recent news and catalysts
- Key metrics: P/E, revenue growth, dividend yield, market cap, analyst target
- Technical context: 52-week range, trend
- 3 specific risk factors
- Stock classification (Growth/Value/Dividend/GARP/Speculative)
- Canadian tax account recommendation (TFSA/RRSP/Non-registered)

---

### Phase 5 — PortfolioConstructionPhase
**File:** `service/research/phases/PortfolioConstructionPhase.java`

**Role:** Synthesizes all previous phases into a cohesive portfolio brief.

**No new searches** — works entirely from Phase 4 output and macro context.

**Output:**
1. How the picks work together (diversification, balance)
2. Position sizing recommendation with dollar amounts
3. Watchlist triggers per stock
4. Portfolio scorecard table
5. Mandatory disclaimer

---

## Extending the System

### Adding a new phase

1. Create a new class implementing `ResearchPhase`:
```java
@Component
@RequiredArgsConstructor
public class MyNewPhase implements ResearchPhase {
    @Override public String getPhaseName() { return "MY_PHASE"; }
    @Override public int getPhaseNumber() { return 6; } // runs after Phase 5
    
    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        // your logic here
    }
}
```
Spring will auto-discover it and `ResearchOrchestrator` will include it in the ordered run.

2. Add a label in the frontend (`src/types/index.ts`):
```ts
export const PHASE_LABELS = {
  ...
  MY_PHASE: 'Phase 6 — My New Phase',
}
```

### Adding a new search provider

1. Implement `SearchService`:
```java
@Service
@ConditionalOnProperty(name = "search.provider", havingValue = "myprovider")
public class MyProviderSearchService implements SearchService { ... }
```

2. Set `search.provider=myprovider` in `application.properties`

### Customizing the Claude prompts

Each phase has its prompts inline in the `execute()` method as Java text blocks.
To externalize them, use Spring's `@Value` or move them to `src/main/resources/prompts/`.

---

## Model Configuration

The Claude model is set in `application.properties`:
```properties
spring.ai.anthropic.chat.model=claude-sonnet-4-6
spring.ai.anthropic.chat.max-tokens=8096
spring.ai.anthropic.chat.temperature=0.3
```

- **Temperature 0.3** — factual, consistent output. Raise to 0.7 for more creative synthesis.
- **Max tokens 8096** — sufficient for detailed phase output. Phase 4 (stock screening) tends to use the most tokens.
- **Model** — `claude-sonnet-4-6` balances quality and cost. Switch to `claude-opus-4-7` for deeper analysis.

---

## Cost Estimates

| Configuration | Est. tokens/run | Est. cost/run |
|---------------|-----------------|---------------|
| Mock search + claude-sonnet-4-6 | ~30K tokens | ~$0.09 |
| Real search (Tavily) + claude-sonnet-4-6 | ~80-120K tokens | ~$0.24-0.36 |
| Real search + claude-opus-4-7 | ~80-120K tokens | ~$1.20-1.80 |

*Prices approximate as of 2026. Check console.anthropic.com for current pricing.*
