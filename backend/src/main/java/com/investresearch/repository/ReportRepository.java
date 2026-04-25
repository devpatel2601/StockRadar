package com.investresearch.repository;

import com.investresearch.model.ResearchReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ResearchReport, String> {

    List<ResearchReport> findAllByOrderByGeneratedAtDesc();
}
