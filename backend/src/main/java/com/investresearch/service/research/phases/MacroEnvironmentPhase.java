package com.investresearch.service.research.phases;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.model.SearchResult;
import com.investresearch.service.ai.ClaudeService;
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
public class MacroEnvironmentPhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;

    @Override
    public String getPhaseName() { return "MACRO_ENVIRONMENT"; }

    @Override
    public int getPhaseNumber() { return 1; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 1 — Macro Environment");

        List<String> queries = List.of(
                "Bank of Canada interest rate decision latest 2026 forward guidance",
                "Federal Reserve interest rate stance impact on Canadian markets 2026",
                "CAD USD exchange rate trend 2026",
                "Canada CPI inflation latest data 2026",
                "TSX composite index current level 52-week range trend",
                "S&P 500 current level trend 2026",
                "Canada US tariffs trade agreements geopolitical events 2026"
        );

        List<SearchResult> results = searchService.searchAll(queries);

        String instructions = """
                Analyze these macroeconomic data points and produce:
                1. A concise "Market Weather Report" paragraph — is the environment risk-on or risk-off?
                2. Three key tailwinds for Canadian investors right now
                3. Three key headwinds for Canadian investors right now
                4. Interest rate trajectory and its implications for different asset classes
                5. CAD/USD context and how it affects cross-border holdings
                """;

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
}
