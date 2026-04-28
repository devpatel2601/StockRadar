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
public class SmartMoneyPhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;
    private final PhaseVariantSelector selector;

    @Override
    public String getPhaseName() { return "SMART_MONEY"; }

    @Override
    public int getPhaseNumber() { return 3; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 3 — Smart Money Tracking");

        List<String> queries = selector.smartMoneyQueries();
        List<SearchResult> results = searchService.searchAll(queries);

        String sectors = profile.getSectorInterests() != null
                ? String.join(", ", profile.getSectorInterests())
                : "general";

        String instructions = """
                At the very start of your response, output EXACTLY this block (no text before it):

                ---SCORES---
                institutionalSentiment=<BULLISH|BEARISH|NEUTRAL>
                institutionalConviction=<integer 0-100>
                ---END SCORES---

                (institutionalConviction: 100 = very high smart money conviction for equities)

                Then synthesize institutional and smart money activity. Produce:

                1. CONVERGENT THEMES — What are 2-3 themes multiple institutions are positioning for?
                2. CONTRARIAN BETS — Any unusual or counter-consensus moves worth noting?
                3. CANADIAN ANGLE — What are CPP, Brookfield, and Fairfax specifically doing?
                4. OVERLAP WITH INVESTOR INTERESTS — How do these moves align with sectors: %s?
                5. NOTABLE EXITS — Any significant selling or portfolio reductions?

                Important: 13F filings have a ~45-day reporting delay. Flag any data where timing matters.
                Clearly label what is confirmed vs speculative based on the sources.

                %s
                """.formatted(sectors, selector.smartMoneyFocus());

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
