package com.investresearch.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorProfile {

    @NotBlank
    private String country;

    @Min(0)
    private double investmentAmount;

    @NotNull
    private RiskTolerance riskTolerance;

    @Min(1)
    private int timelineYears;

    private List<String> sectorInterests;

    @NotNull
    private InvestmentGoal goal;

    private List<AccountType> accounts;

    private List<String> currentHoldings;

    private ScreeningStrategy screeningStrategy;

    public enum ScreeningStrategy {
        CANSLIM, VCP, GROWTH, VALUE_DIVIDEND, BALANCED
    }

    public enum RiskTolerance {
        CONSERVATIVE, MODERATE, AGGRESSIVE
    }

    public enum InvestmentGoal {
        GROWTH, INCOME, PRESERVATION, SPECULATIVE
    }

    public enum AccountType {
        TFSA, RRSP, NON_REGISTERED
    }
}
