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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchOrchestrator orchestrator;
    private final ReportStore reportStore;

    /**
     * Run a full 5-phase investment analysis.
     * Note: this is synchronous and can take 30-120 seconds depending on the AI + search calls.
     * Future: migrate to async with SSE streaming.
     */
    @PostMapping("/analyze")
    public ResponseEntity<ResearchReport> analyze(@Valid @RequestBody InvestorProfile profile) {
        log.info("POST /analyze — country={}, risk={}, goal={}", profile.getCountry(), profile.getRiskTolerance(), profile.getGoal());
        ResearchReport report = orchestrator.runFullAnalysis(profile);
        reportStore.save(report);
        return ResponseEntity.ok(report);
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
