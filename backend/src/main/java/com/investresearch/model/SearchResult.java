package com.investresearch.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String query;
    private String title;
    private String snippet;
    private String url;
    private String fullContent;
    private LocalDateTime retrievedAt;
}
