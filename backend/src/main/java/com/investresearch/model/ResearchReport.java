package com.investresearch.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ResearchReport {
    private String id;
    private LocalDateTime generatedAt;
    private InvestorProfile profile;
    private Map<String, PhaseResult> phases;
    private String finalBriefing;
    private ReportStatus status;

    public enum ReportStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
