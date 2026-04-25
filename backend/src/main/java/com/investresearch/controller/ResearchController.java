package com.investresearch.controller;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.ResearchReport;
import com.investresearch.service.research.ResearchOrchestrator;
import com.investresearch.store.ReportStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchOrchestrator orchestrator;
    private final ReportStore reportStore;

    /**
     * Kick off a 5-phase analysis asynchronously.
     * Returns 202 Accepted immediately with a PENDING report.
     * Poll GET /reports/{id} until status == COMPLETED or FAILED.
     */
    @PostMapping("/analyze")
    public ResponseEntity<ResearchReport> analyze(@Valid @RequestBody InvestorProfile profile) {
        String reportId = UUID.randomUUID().toString();
        log.info("POST /analyze — reportId={} country={} risk={}", reportId, profile.getCountry(), profile.getRiskTolerance());

        ResearchReport report = ResearchReport.builder()
                .id(reportId)
                .generatedAt(LocalDateTime.now())
                .profile(profile)
                .phases(new LinkedHashMap<>())
                .status(ResearchReport.ReportStatus.PENDING)
                .build();

        reportStore.save(report);
        orchestrator.runAnalysisAsync(reportId, profile);

        return ResponseEntity.accepted().body(report);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ResearchReport>> listReports() {
        return ResponseEntity.ok(reportStore.findAll());
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ResearchReport> getReport(@PathVariable String id) {
        return reportStore.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        reportStore.delete(id);
        return ResponseEntity.noContent().build();
    }
}
