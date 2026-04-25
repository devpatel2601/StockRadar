package com.investresearch.service.search;

import com.investresearch.model.SearchResult;

import java.util.List;

public interface SearchService {
    List<SearchResult> search(String query);
    List<SearchResult> searchAll(List<String> queries);
}
