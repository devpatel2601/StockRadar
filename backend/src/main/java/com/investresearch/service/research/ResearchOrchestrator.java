package com.investresearch.service.research;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.model.ResearchReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchOrchestrator {

    private final List<ResearchPhase> phases;

    public ResearchReport runFullAnalysis(InvestorProfile profile) {
        String reportId = UUID.randomUUID().toString();
        log.info("Starting full analysis — reportId={}, investor={}/{}", reportId, profile.getCountry(), profile.getRiskTolerance());

        Map<String, PhaseResult> results = new LinkedHashMap<>();

        List<ResearchPhase> orderedPhases = phases.stream()
                .sorted((a, b) -> Integer.compare(a.getPhaseNumber(), b.getPhaseNumber()))
                .toList();

        for (int i = 0; i < orderedPhases.size(); i++) {
            ResearchPhase phase = orderedPhases.get(i);
            if (i > 0) {
                try { Thread.sleep(8000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
            log.info("Running Phase {} — {}", phase.getPhaseNumber(), phase.getPhaseName());
            try {
                PhaseResult result = phase.execute(profile, results);
                results.put(phase.getPhaseName(), result);
                log.info("Phase {} complete", phase.getPhaseName());
            } catch (Exception e) {
                log.error("Phase {} failed: {}", phase.getPhaseName(), e.getMessage(), e);
                results.put(phase.getPhaseName(), PhaseResult.builder()
                        .phaseName(phase.getPhaseName())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .completedAt(LocalDateTime.now())
                        .build());
            }
        }

        String finalBriefing = results.getOrDefault("PORTFOLIO_CONSTRUCTION",
                PhaseResult.builder().synthesis("Analysis incomplete.").build()).getSynthesis();

        log.info("Analysis complete — reportId={}", reportId);

        return ResearchReport.builder()
                .id(reportId)
                .generatedAt(LocalDateTime.now())
                .profile(profile)
                .phases(results)
                .finalBriefing(finalBriefing)
                .status(ResearchReport.ReportStatus.COMPLETED)
                .build();
    }
}
