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
public class SectorPulsePhase implements ResearchPhase {

    private final SearchService searchService;
    private final ClaudeService claudeService;
    private final PhaseVariantSelector selector;

    @Override
    public String getPhaseName() { return "SECTOR_PULSE"; }

    @Override
    public int getPhaseNumber() { return 2; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 2 — Sector Pulse");

        List<String> all = (profile.getSectorInterests() != null && !profile.getSectorInterests().isEmpty())
                ? profile.getSectorInterests()
                : List.of("Technology", "Energy", "Financials");
        List<String> sectors = all.size() > 5 ? all.subList(0, 5) : all;
        if (all.size() > 5) log.info("Capping sectors {}->{} to stay within token limits", all.size(), sectors.size());

        List<String> queries = selector.sectorQueries(sectors);
        List<SearchResult> results = searchService.searchAll(queries);

        String sectorList = String.join(", ", sectors);
        String instructions = """
                For each of these sectors: %s

                Produce a scorecard table with these columns:
                | Sector | Momentum (STRONG/NEUTRAL/WEAK) | Key Catalyst (3-6 months) | Key Risk | In/Out of Favor? |

                Then write 2-3 sentences of commentary per sector explaining the current environment,
                noting any TSX-listed ETFs or benchmarks as reference.

                %s
                """.formatted(sectorList, selector.sectorFocus());

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
