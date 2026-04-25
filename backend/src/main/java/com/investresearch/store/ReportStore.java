package com.investresearch.store;

import com.investresearch.model.ResearchReport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportStore {

    private final Map<String, ResearchReport> store = new ConcurrentHashMap<>();

    public void save(ResearchReport report) {
        store.put(report.getId(), report);
    }

    public Optional<ResearchReport> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<ResearchReport> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(String id) {
        store.remove(id);
    }
}
