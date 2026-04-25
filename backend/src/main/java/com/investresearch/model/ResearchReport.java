package com.investresearch.model;

import com.investresearch.persistence.InvestorProfileConverter;
import com.investresearch.persistence.PhasesMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "research_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchReport {

    @Id
    private String id;

    private LocalDateTime generatedAt;

    @Convert(converter = InvestorProfileConverter.class)
    @Column(columnDefinition = "TEXT")
    private InvestorProfile profile;

    @Convert(converter = PhasesMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, PhaseResult> phases;

    @Column(columnDefinition = "TEXT")
    private String finalBriefing;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    public enum ReportStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
