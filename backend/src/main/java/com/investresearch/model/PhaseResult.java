package com.investresearch.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseResult {
    private String phaseName;
    private List<String> searchQueries;
    private List<SearchResult> rawResults;
    private String synthesis;
    private LocalDateTime completedAt;
    private boolean success;
    private String errorMessage;
}
