package com.investresearch.store;

import com.investresearch.model.ResearchReport;
import com.investresearch.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportStore {

    private final ReportRepository repository;

    public void save(ResearchReport report) {
        repository.save(report);
    }

    public Optional<ResearchReport> findById(String id) {
        return repository.findById(id);
    }

    public List<ResearchReport> findAll() {
        return repository.findAllByOrderByGeneratedAtDesc();
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
