package com.investresearch.service.research.phases;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.model.SearchResult;
import com.investresearch.service.ai.ClaudeService;
import com.investresearch.service.research.PhaseScoreParser;
import com.investresearch.service.research.PhaseVariantSelector;
import com.investresearch.service.research.ResearchPhase;
import com.investresearch.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockScreeningPhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;
    private final PhaseVariantSelector selector;

    @Override
    public String getPhaseName() { return "STOCK_SCREENING"; }

    @Override
    public int getPhaseNumber() { return 4; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 4 — Stock Screening");

        List<String> all = (profile.getSectorInterests() != null && !profile.getSectorInterests().isEmpty())
                ? profile.getSectorInterests()
                : List.of("Technology", "Energy");
        List<String> sectors = all.size() > 5 ? all.subList(0, 5) : all;

        InvestorProfile.ScreeningStrategy strategy =
                profile.getScreeningStrategy() != null
                        ? profile.getScreeningStrategy()
                        : InvestorProfile.ScreeningStrategy.BALANCED;

        log.info("Screening strategy: {}", strategy);

        List<String> queries = new ArrayList<>(selector.stockQueries(sectors, profile.getRiskTolerance().name()));
        queries.addAll(strategyExtraQueries(strategy));

        List<SearchResult> results = searchService.searchAll(queries);

        String macroSummary  = truncate(previousPhases.getOrDefault("MACRO_ENVIRONMENT", emptyPhase()).getSynthesis(), 400);
        String sectorSummary = truncate(previousPhases.getOrDefault("SECTOR_PULSE",      emptyPhase()).getSynthesis(), 400);
        String smartSummary  = truncate(previousPhases.getOrDefault("SMART_MONEY",       emptyPhase()).getSynthesis(), 400);

        boolean isCanslim = strategy == InvestorProfile.ScreeningStrategy.CANSLIM;

        String instructions = """
                At the very start of your response, output EXACTLY this block (no text before it):

                ---SCORES---
                stock_<TICKER>=<conviction integer 0-100>
                %s(one line per final pick)
                ---END SCORES---

                Then using the search results and context below, identify 8-12 candidate stocks,
                then narrow to the 3-5 best fits for this investor profile:

                INVESTOR PROFILE:
                - Risk Tolerance: %s
                - Timeline: %d years
                - Goal: %s
                - Sectors of interest: %s
                - Accounts: %s
                - Screening Strategy: %s
                - Preference: TSX-listed stocks preferred to avoid currency/withholding tax complexity

                %s

                For each of the 3-5 final picks, provide a structured analysis with:
                STOCK: [Ticker] — [Company Name]
                Exchange: [TSX / NYSE / NASDAQ]
                Current Price: [from search, note date]
                Sector: [sector]
                WHY IT FITS THIS PROFILE: [1-2 sentences]
                SMART MONEY ACTIVITY: [any institutional holders or insider buying]
                RECENT NEWS & CATALYSTS: [2-3 items from last 30 days]
                KEY METRICS: P/E, Revenue Growth YoY, Dividend Yield, Market Cap, Analyst Consensus + price target
                TECHNICAL: 52-week range, current price position, 3-month trend
                RISK FACTORS: [3 specific risks — include valuation risk if P/E > 40]
                STOCK TYPE: [Growth / Value / Dividend / GARP / Speculative / Turnaround]
                TAX ACCOUNT: [TFSA / RRSP / Non-registered recommendation with reason]

                Macro context: %s
                Sector context: %s
                Smart money context: %s

                %s
                """.formatted(
                isCanslim ? "canslim_<TICKER>=<criteria met integer 0-7>\n                " : "",
                profile.getRiskTolerance(), profile.getTimelineYears(), profile.getGoal(),
                sectors, profile.getAccounts(), strategy,
                methodologyPrompt(strategy),
                macroSummary, sectorSummary, smartSummary,
                selector.stockFocus()
        );

        String raw = claudeService.synthesizePhase(getPhaseName(), instructions, results);
        PhaseScoreParser.ParsedPhase parsed = PhaseScoreParser.parse(raw);

        return PhaseResult.builder()
                .phaseName(getPhaseName())
                .searchQueries(queries)
                .rawResults(results)
                .synthesis(parsed.synthesis())
                .scores(parsed.scores())
                .completedAt(LocalDateTime.now())
                .success(true)
                .build();
    }

    private String methodologyPrompt(InvestorProfile.ScreeningStrategy strategy) {
        return switch (strategy) {
            case CANSLIM -> """
                    Apply O'Neil's CANSLIM methodology and score each final pick (0-7 criteria met):
                    C = Current quarterly EPS growth > 25% YoY
                    A = Annual EPS growth > 25% for 3+ years
                    N = New product/service, management, or within 5% of 52-week high
                    S = Supply/demand — rising volume on up days, float < 50M shares preferred
                    L = Leader vs laggard — relative strength vs TSX > 70th percentile
                    I = Institutional sponsorship from 3-10 quality funds increasing their stakes
                    M = Market direction uptrend (assessed from Phase 1 macro regime)
                    List which CANSLIM criteria each pick meets and misses.
                    """;
            case VCP -> """
                    Apply Minervini's Volatility Contraction Pattern (VCP) methodology:
                    - Stock must be in a clear Stage 2 uptrend (price > 150-day MA, 150-day MA > 200-day MA)
                    - Price makes a series of progressively tighter contractions (each pullback smaller than last)
                    - Volume declines during each contraction (constructive consolidation, not distribution)
                    - Final tight base < 10% depth, near 52-week high
                    - Identify the pivot/buy point (top of final consolidation)
                    Prioritize TSX stocks showing VCP setup characteristics.
                    """;
            case GROWTH -> """
                    Apply Quality Growth screening criteria:
                    - Revenue growth > 20% YoY (or > 15% for large caps)
                    - Expanding operating margins over past 4 quarters
                    - Dominant market position with defensible competitive moat
                    - Strong balance sheet (net cash preferred, D/E < 0.5)
                    - EPS strongly growing or inflecting positive
                    - Large total addressable market with runway for 3-5+ years of growth
                    """;
            case VALUE_DIVIDEND -> """
                    Apply Value & Dividend Growth screening criteria:
                    - P/E ratio below sector median or 5-year historical average
                    - Dividend yield > 2.5% with payout ratio < 75%
                    - 3+ consecutive years of dividend growth (Canadian Dividend Aristocrat preferred)
                    - Free cash flow yield > dividend yield (dividend fully covered by FCF)
                    - Low debt: D/E < 1.5, interest coverage > 5x
                    - No dividend cuts in the past 5 years
                    Prioritize RRSP-eligible holdings for foreign dividend withholding tax optimization.
                    """;
            case BALANCED -> "";
        };
    }

    private List<String> strategyExtraQueries(InvestorProfile.ScreeningStrategy strategy) {
        return switch (strategy) {
            case CANSLIM -> List.of(
                    "CANSLIM stocks TSX 52-week high breakout earnings acceleration institutional buying 2026",
                    "TSX stocks EPS growth 25 percent institutional sponsorship high relative strength 2026"
            );
            case VCP -> List.of(
                    "TSX stocks stage 2 uptrend tight consolidation momentum breakout 2026",
                    "Canadian stocks volatility contraction pattern near 52-week high low volume base 2026"
            );
            case GROWTH -> List.of(
                    "TSX high growth stocks revenue acceleration market leaders expanding margins 2026",
                    "Canadian growth companies revenue beats guidance raise large TAM 2026"
            );
            case VALUE_DIVIDEND -> List.of(
                    "TSX dividend aristocrats value stocks FCF yield low PE high yield 2026",
                    "Canadian dividend growth stocks payout ratio covered consecutive increases 2026"
            );
            case BALANCED -> List.of();
        };
    }

    private PhaseResult emptyPhase() {
        return PhaseResult.builder().synthesis("No data available.").build();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
