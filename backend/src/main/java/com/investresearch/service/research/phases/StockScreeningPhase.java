package com.investresearch.service.research.phases;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.model.SearchResult;
import com.investresearch.service.ai.ClaudeService;
import com.investresearch.service.research.PhaseVariantSelector;
import com.investresearch.service.research.ResearchPhase;
import com.investresearch.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

        List<String> queries = selector.stockQueries(sectors, profile.getRiskTolerance().name());
        List<SearchResult> results = searchService.searchAll(queries);

        String macroSummary   = truncate(previousPhases.getOrDefault("MACRO_ENVIRONMENT", emptyPhase()).getSynthesis(), 400);
        String sectorSummary  = truncate(previousPhases.getOrDefault("SECTOR_PULSE",      emptyPhase()).getSynthesis(), 400);
        String smartSummary   = truncate(previousPhases.getOrDefault("SMART_MONEY",       emptyPhase()).getSynthesis(), 400);

        String instructions = """
                Using the search results and context below, identify 8-12 candidate stocks,
                then narrow to the 3-5 best fits for this investor profile:

                INVESTOR PROFILE:
                - Risk Tolerance: %s
                - Timeline: %d years
                - Goal: %s
                - Sectors of interest: %s
                - Accounts: %s
                - Preference: TSX-listed stocks preferred to avoid currency/withholding tax complexity

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
                profile.getRiskTolerance(), profile.getTimelineYears(), profile.getGoal(),
                sectors, profile.getAccounts(),
                macroSummary, sectorSummary, smartSummary,
                selector.stockFocus()
        );

        String synthesis = claudeService.synthesizePhase(getPhaseName(), instructions, results);

        return PhaseResult.builder()
                .phaseName(getPhaseName())
                .searchQueries(queries)
                .rawResults(results)
                .synthesis(synthesis)
                .completedAt(LocalDateTime.now())
                .success(true)
                .build();
    }

    private PhaseResult emptyPhase() {
        return PhaseResult.builder().synthesis("No data available.").build();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
