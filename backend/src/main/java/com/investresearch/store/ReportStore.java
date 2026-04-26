package com.investresearch.store;

import com.investresearch.model.ResearchReport;
import com.investresearch.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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

    public Optional<ResearchReport> findByIdAndUserId(String id, String userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    public List<ResearchReport> findByUserId(String userId) {
        return repository.findByUserIdOrderByGeneratedAtDesc(userId);
    }

    public void deleteByIdAndUserId(String id, String userId) {
        ResearchReport report = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repository.delete(report);
    }
}
