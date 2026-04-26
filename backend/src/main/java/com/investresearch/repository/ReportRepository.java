package com.investresearch.repository;

import com.investresearch.model.ResearchReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ResearchReport, String> {

    List<ResearchReport> findByUserIdOrderByGeneratedAtDesc(String userId);

    Optional<ResearchReport> findByIdAndUserId(String id, String userId);
}
