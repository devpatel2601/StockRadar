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
public class SmartMoneyPhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;

    @Override
    public String getPhaseName() { return "SMART_MONEY"; }

    @Override
    public int getPhaseNumber() { return 3; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 3 — Smart Money Tracking");

        List<String> queries = List.of(
                "13F filings top hedge funds latest quarter 2026 Berkshire Hathaway Bridgewater Citadel",
                "Warren Buffett Berkshire Hathaway stock purchases Q1 2026",
                "Bill Ackman Pershing Square portfolio changes 2026",
                "Michael Burry Scion 13F latest holdings 2026",
                "Cathie Wood ARK Invest trades buys sells 2026",
                "CPP Investments CPPIB recent investments portfolio 2026",
                "Brookfield Asset Management acquisitions investments 2026",
                "Prem Watsa Fairfax Financial holdings investments 2026",
                "TSX insider buying largest transactions this month 2026",
                "Canadian institutional investors top picks 2026"
        );

        List<SearchResult> results = searchService.searchAll(queries);

        String sectors = profile.getSectorInterests() != null
                ? String.join(", ", profile.getSectorInterests())
                : "general";

        String instructions = """
                Synthesize institutional and smart money activity. Produce:

                1. CONVERGENT THEMES — What are 2-3 themes multiple institutions are positioning for?
                2. CONTRARIAN BETS — Any unusual or counter-consensus moves worth noting?
                3. CANADIAN ANGLE — What are CPP, Brookfield, and Fairfax specifically doing?
                4. OVERLAP WITH INVESTOR INTERESTS — How do these moves align with sectors: %s?
                5. NOTABLE EXITS — Any significant selling or portfolio reductions?

                Important: 13F filings have a ~45-day reporting delay. Flag any data where timing matters.
                Clearly label what is confirmed vs speculative based on the sources.
                """.formatted(sectors);

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
