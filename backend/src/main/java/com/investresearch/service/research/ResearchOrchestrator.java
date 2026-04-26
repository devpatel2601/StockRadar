package com.investresearch.service.research;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.model.ResearchReport;
import com.investresearch.store.ReportStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchOrchestrator {

    private final List<ResearchPhase> phases;
    private final ReportStore reportStore;

    @Async("analysisExecutor")
    public CompletableFuture<Void> runAnalysisAsync(String reportId, InvestorProfile profile) {
        log.info("Async analysis started — reportId={}", reportId);

        ResearchReport report = reportStore.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        report.setStatus(ResearchReport.ReportStatus.IN_PROGRESS);
        reportStore.save(report);

        Map<String, PhaseResult> results = new LinkedHashMap<>();

        List<ResearchPhase> orderedPhases = phases.stream()
                .sorted((a, b) -> Integer.compare(a.getPhaseNumber(), b.getPhaseNumber()))
                .toList();

        for (int i = 0; i < orderedPhases.size(); i++) {
            ResearchPhase phase = orderedPhases.get(i);

            if (i > 0) {
                try {
                    Thread.sleep(8_000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("Running Phase {} — {}", phase.getPhaseNumber(), phase.getPhaseName());
            try {
                PhaseResult result = phase.execute(profile, results);
                results.put(phase.getPhaseName(), result);
            } catch (Exception e) {
                log.error("Phase {} failed: {}", phase.getPhaseName(), e.getMessage(), e);
                results.put(phase.getPhaseName(), PhaseResult.builder()
                        .phaseName(phase.getPhaseName())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .completedAt(LocalDateTime.now())
                        .build());
            }

            // Persist after each phase so the frontend sees real-time progress
            report.setPhases(new LinkedHashMap<>(results));
            reportStore.save(report);
            log.info("Phase {} saved — reportId={}", phase.getPhaseName(), reportId);
        }

        String finalBriefing = results.getOrDefault("PORTFOLIO_CONSTRUCTION",
                PhaseResult.builder().synthesis("Analysis incomplete.").build()).getSynthesis();

        report.setFinalBriefing(finalBriefing);
        report.setStatus(ResearchReport.ReportStatus.COMPLETED);
        reportStore.save(report);

        log.info("Async analysis complete — reportId={}", reportId);
        return CompletableFuture.completedFuture(null);
    }
}
