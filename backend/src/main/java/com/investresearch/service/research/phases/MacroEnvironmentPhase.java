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
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MacroEnvironmentPhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;
    private final PhaseVariantSelector selector;

    @Override
    public String getPhaseName() { return "MACRO_ENVIRONMENT"; }

    @Override
    public int getPhaseNumber() { return 1; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 1 — Macro Environment");

        List<String> queries = selector.macroQueries();
        List<SearchResult> results = searchService.searchAll(queries);

        String instructions = """
                At the very start of your response, output EXACTLY this block (no text before it):

                ---SCORES---
                regime=<BULL_MARKET|BEAR_MARKET|TRANSITION|NEUTRAL>
                riskSentiment=<RISK_ON|RISK_OFF|NEUTRAL>
                macroScore=<integer 0-100>
                ---END SCORES---

                (macroScore: 100 = ideal conditions for equity growth, 0 = severe recession/crisis)

                Then write your full analysis:
                1. A concise "Market Weather Report" paragraph — is the environment risk-on or risk-off?
                2. Three key tailwinds for Canadian investors right now
                3. Three key headwinds for Canadian investors right now
                4. Interest rate trajectory and its implications for different asset classes
                5. CAD/USD context and how it affects cross-border holdings

                %s
                """.formatted(selector.macroFocus());

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
}
