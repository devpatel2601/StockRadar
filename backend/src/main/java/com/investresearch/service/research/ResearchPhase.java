package com.investresearch.service.research;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;

import java.util.Map;

public interface ResearchPhase {
    String getPhaseName();
    int getPhaseNumber();
    PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases);
}
