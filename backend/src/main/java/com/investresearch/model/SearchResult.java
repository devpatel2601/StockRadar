package com.investresearch.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SearchResult {
    private String query;
    private String title;
    private String snippet;
    private String url;
    private String fullContent;
    private LocalDateTime retrievedAt;
}
