package com.investresearch.service.research;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides daily-rotating query pools and focus-angle instructions for each research phase.
 *
 * Slot selection: LocalDate.getDayOfYear() % poolSize
 *   — same day  → same slot → cache hits on both search and AI layers
 *   — next day  → different slot → fresh queries → fresh AI output
 *
 * Query pools rotate every N days (N = pool count, currently 3).
 * Focus angles rotate every M days (M = angle count, currently 4).
 * They are independent, so the combination cycles every lcm(N,M) = 12 days.
 */
@Slf4j
@Component
public class PhaseVariantSelector {

    private int slot(int poolSize) {
        return LocalDate.now().getDayOfYear() % poolSize;
    }

    // ── Phase 1: Macro Environment ────────────────────────────────────────────

    private static final List<List<String>> MACRO_POOLS = List.of(
        List.of(
            "Bank of Canada interest rate decision latest 2026 forward guidance",
            "Federal Reserve interest rate stance impact on Canadian markets 2026",
            "CAD USD exchange rate trend 2026",
            "Canada CPI inflation latest data 2026",
            "TSX composite index current level 52-week range trend",
            "S&P 500 current level trend 2026",
            "Canada US tariffs trade agreements geopolitical events 2026"
        ),
        List.of(
            "Bank of Canada monetary policy GDP growth forecast 2026",
            "US Federal Reserve rate cuts timeline 2026 economic outlook",
            "crude oil WTI Brent price forecast Canadian energy sector 2026",
            "Canada GDP economic indicators Q1 Q2 2026",
            "TSX energy gold mining sector performance 2026",
            "global bond yields US 10 year treasury equity impact 2026",
            "Canada housing market consumer debt spending trajectory 2026"
        ),
        List.of(
            "Bank of Canada financial stability credit conditions report 2026",
            "US dollar DXY strength impact global equity markets 2026",
            "Canada unemployment jobs labour market data 2026",
            "US Canada trade policy tariffs economic impact sectors 2026",
            "TSX composite valuation P/E ratio historical comparison 2026",
            "yield curve recession probability Canada US 2026",
            "gold silver base metals commodity supercycle Canadian miners 2026"
        )
    );

    private static final List<String> MACRO_ANGLES = List.of(
        "**Analysis focus:** Emphasize interest rate trajectory and its second-order effects on TSX sectors (banks, utilities, REITs).",
        "**Analysis focus:** Emphasize commodity price trends (oil, gold, metals) and tailwinds/headwinds for resource-heavy TSX sectors.",
        "**Analysis focus:** Emphasize consumer health — employment, housing, debt levels — and implications for retail and financial stocks.",
        "**Analysis focus:** Emphasize CAD/USD dynamics, trade flows, and how exchange rate shifts affect cross-border Canadian holdings."
    );

    public List<String> macroQueries() {
        int s = slot(MACRO_POOLS.size());
        log.debug("Phase 1 using query pool {} (day {})", s, LocalDate.now().getDayOfYear());
        return MACRO_POOLS.get(s);
    }

    public String macroFocus() {
        return MACRO_ANGLES.get(slot(MACRO_ANGLES.size()));
    }

    // ── Phase 2: Sector Pulse ─────────────────────────────────────────────────

    private static final List<List<String>> SECTOR_QUERY_POOLS = List.of(
        List.of(
            "%s stocks Canada TSX outlook 2026",
            "%s ETF performance returns 2026",
            "%s sector catalysts risks next 6 months 2026"
        ),
        List.of(
            "%s sector Canada undervalued stocks analyst buy recommendation 2026",
            "%s companies revenue earnings growth beat surprise TSX 2026",
            "%s industry trends disruption risks opportunities Canada 2026"
        ),
        List.of(
            "top %s TSX stocks insider buying institutional accumulation 2026",
            "%s sector Canadian ETF top holdings performance 2026",
            "%s companies dividend growth earnings momentum Canada 2026"
        )
    );

    private static final List<String> SECTOR_ANGLES = List.of(
        "**Sector lens:** Identify the single strongest and weakest sector. Compare current valuation to 5-year historical P/E averages.",
        "**Sector lens:** Prioritize earnings quality and revenue growth sustainability. Flag sectors where growth may be decelerating.",
        "**Sector lens:** Identify the most important upcoming catalysts (earnings, regulatory, macro) that could move each sector in 90 days.",
        "**Sector lens:** Emphasize institutional capital flow direction — which sectors are seeing money rotation in vs out?"
    );

    public List<String> sectorQueries(List<String> sectors) {
        List<String> templates = SECTOR_QUERY_POOLS.get(slot(SECTOR_QUERY_POOLS.size()));
        List<String> queries = new ArrayList<>();
        for (String sector : sectors) {
            for (String t : templates) {
                queries.add(t.formatted(sector));
            }
        }
        return queries;
    }

    public String sectorFocus() {
        return SECTOR_ANGLES.get(slot(SECTOR_ANGLES.size()));
    }

    // ── Phase 3: Smart Money ──────────────────────────────────────────────────

    private static final List<List<String>> SMART_MONEY_POOLS = List.of(
        List.of(
            "13F filings top hedge funds latest quarter 2026 Berkshire Hathaway Bridgewater Citadel",
            "Warren Buffett Berkshire Hathaway stock purchases 2026",
            "Bill Ackman Pershing Square portfolio changes 2026",
            "Michael Burry Scion 13F latest holdings 2026",
            "Cathie Wood ARK Invest trades buys sells 2026",
            "CPP Investments CPPIB recent investments portfolio 2026",
            "Brookfield Asset Management acquisitions investments 2026",
            "Prem Watsa Fairfax Financial holdings investments 2026",
            "TSX insider buying largest transactions 2026",
            "Canadian institutional investors top picks 2026"
        ),
        List.of(
            "Elliott Management activist investor positions new stakes 2026",
            "Temasek GIC Abu Dhabi sovereign wealth fund Canada investments 2026",
            "Blackstone KKR Apollo private equity Canada deal activity 2026",
            "Ontario Teachers Pension Plan OMERS portfolio moves 2026",
            "David Einhorn Greenlight Stanley Druckenmiller positions 2026",
            "CPPIB OTPP equity allocation rebalancing Q1 2026",
            "Brookfield Infrastructure Partners Renewable Energy acquisitions 2026",
            "TSX highest short interest stocks squeeze candidates 2026",
            "hedge fund sector rotation technology energy healthcare 2026",
            "SEDAR insider trading executive buying selling TSX 2026"
        ),
        List.of(
            "Ray Dalio Bridgewater All Weather macro positioning 2026",
            "global asset allocation institutional survey equities bonds 2026",
            "growth vs value style rotation institutional funds 2026",
            "Fairfax Financial contrarian bets macro portfolio 2026",
            "CPP Investments private equity real assets infrastructure 2026",
            "hedge fund net exposure risk positioning equity 2026",
            "pension fund liability driven Canada bond equity shift 2026",
            "M&A Canada cross border acquisition deal flow 2026",
            "gold mining alternative assets institutional accumulation 2026",
            "long only fund manager Canada top sector overweight 2026"
        )
    );

    private static final List<String> SMART_MONEY_ANGLES = List.of(
        "**Smart money focus:** Emphasize position sizing changes — where are institutions building conviction vs trimming holdings?",
        "**Smart money focus:** Emphasize new positions initiated — what new themes are smart money investors gaining exposure to?",
        "**Smart money focus:** Emphasize Canada-specific institutional moves — CPP, OTPP, Brookfield, Fairfax — and what their positioning signals.",
        "**Smart money focus:** Emphasize contrarian and counter-consensus bets — who is zigging while market consensus zags, and why?"
    );

    public List<String> smartMoneyQueries() {
        int s = slot(SMART_MONEY_POOLS.size());
        log.debug("Phase 3 using query pool {} (day {})", s, LocalDate.now().getDayOfYear());
        return SMART_MONEY_POOLS.get(s);
    }

    public String smartMoneyFocus() {
        return SMART_MONEY_ANGLES.get(slot(SMART_MONEY_ANGLES.size()));
    }

    // ── Phase 4: Stock Screening ──────────────────────────────────────────────

    private static final List<List<String>> STOCK_SECTOR_POOLS = List.of(
        List.of(
            "best %s stocks TSX Canada buy recommendation 2026",
            "top %s dividend growth stocks Canada 2026"
        ),
        List.of(
            "%s stocks undervalued low P/E strong FCF balance sheet Canada TSX 2026",
            "%s sector Canadian ETF top holdings performance 2026"
        ),
        List.of(
            "top %s companies Canada revenue earnings growth beat 2026",
            "%s stocks analyst upgrade increased price target TSX 2026"
        )
    );

    private static final List<List<String>> STOCK_GENERIC_POOLS = List.of(
        List.of(
            "top TSX stocks undervalued analyst buy 2026 %s",
            "Canadian stocks strong earnings momentum 2026"
        ),
        List.of(
            "TSX value stocks low P/E dividend yield consistent earnings 2026 %s",
            "Canadian blue chip defensive quality stocks 2026"
        ),
        List.of(
            "Canadian GARP growth reasonable price quality stocks 2026 %s",
            "TSX mid cap small cap breakout momentum candidates 2026"
        )
    );

    private static final List<String> STOCK_ANGLES = List.of(
        "**Screening focus:** Prioritize fundamental value — P/E below sector median, strong FCF yield, clean balance sheet (D/E < 1.5).",
        "**Screening focus:** Prioritize momentum and technical strength — stocks at or near 52-week highs with strong relative strength vs TSX.",
        "**Screening focus:** Prioritize dividend sustainability — yield above 2.5%, payout ratio < 75%, 3+ consecutive years of dividend growth.",
        "**Screening focus:** Prioritize upcoming catalysts — earnings in next 30 days, regulatory decisions, product launches, or guidance raises."
    );

    public List<String> stockQueries(List<String> sectors, String riskTolerance) {
        int s = slot(STOCK_SECTOR_POOLS.size());
        List<String> sectorTemplates  = STOCK_SECTOR_POOLS.get(s);
        List<String> genericTemplates = STOCK_GENERIC_POOLS.get(s);
        String risk = riskTolerance.toLowerCase();

        List<String> queries = new ArrayList<>();
        for (String sector : sectors) {
            for (String t : sectorTemplates) {
                queries.add(t.formatted(sector));
            }
        }
        for (String t : genericTemplates) {
            queries.add(t.contains("%s") ? t.formatted(risk) : t);
        }
        return queries;
    }

    public String stockFocus() {
        return STOCK_ANGLES.get(slot(STOCK_ANGLES.size()));
    }

    // ── Phase 5: Portfolio Construction ──────────────────────────────────────

    private static final List<String> PORTFOLIO_ANGLES = List.of(
        "**Portfolio approach:** Build a core-satellite structure — ~70% high-conviction core positions, ~30% higher-upside satellite.",
        "**Portfolio approach:** Apply a barbell strategy — pair 1-2 defensive dividend payers with 1-2 higher-growth positions for asymmetric return.",
        "**Portfolio approach:** Use equal-weight allocation with specific numeric rebalancing triggers for each position.",
        "**Portfolio approach:** Use conviction-weighted sizing — assign the highest weight to the pick with the strongest multi-factor support across fundamentals, smart money, and catalysts."
    );

    public String portfolioFocus() {
        return PORTFOLIO_ANGLES.get(slot(PORTFOLIO_ANGLES.size()));
    }
}
