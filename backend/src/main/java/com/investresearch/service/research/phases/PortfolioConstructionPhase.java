package com.investresearch.service.research.phases;

import com.investresearch.model.InvestorProfile;
import com.investresearch.model.PhaseResult;
import com.investresearch.service.ai.ClaudeService;
import com.investresearch.service.research.ResearchPhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioConstructionPhase implements ResearchPhase {

    private final ClaudeService claudeService;

    @Override
    public String getPhaseName() { return "PORTFOLIO_CONSTRUCTION"; }

    @Override
    public int getPhaseNumber() { return 5; }

    @Override
    public PhaseResult execute(InvestorProfile profile, Map<String, PhaseResult> previousPhases) {
        log.info("Phase 5 — Portfolio Construction & Final Briefing");

        String stockAnalysis = previousPhases.getOrDefault("STOCK_SCREENING",
                PhaseResult.builder().synthesis("No stock analysis available.").build()).getSynthesis();
        String macroContext = previousPhases.getOrDefault("MACRO_ENVIRONMENT",
                PhaseResult.builder().synthesis("").build()).getSynthesis();

        String systemPrompt = """
                You are an expert investment research analyst and portfolio strategist.
                You are an EDUCATOR, not a financial advisor.
                Frame all analysis as "here is what the data shows" — never "you should buy/sell."
                Be honest about risks and valuation concerns.
                Always end with the disclaimer: "This is educational research, not financial advice.
                Consult a qualified financial advisor before making investment decisions."
                """;

        String userContent = """
                Produce a complete Portfolio Construction Brief for this investor.

                INVESTOR PROFILE:
                - Country: %s
                - Investment Amount: $%.0f CAD
                - Risk Tolerance: %s
                - Timeline: %d years
                - Goal: %s
                - Accounts: %s
                - Current Holdings: %s

                STOCK ANALYSIS (from Phase 4):
                %s

                MACRO CONTEXT:
                %s

                Produce the following sections:

                ## Portfolio Construction Note

                ### 1. How These Picks Work Together
                Describe the diversification across sectors, risk levels, and growth vs income balance.
                Note any concentration risk if multiple picks are in the same sector.

                ### 2. Suggested Position Sizing
                Based on risk tolerance (%s), suggest whether to use:
                - Equal weight (simpler, less conviction required), or
                - Conviction-weighted (higher allocation to highest-confidence picks)
                Provide example dollar allocations for the $%.0f investment amount.

                ### 3. Watchlist Triggers
                For each stock pick, list specific events or price levels that would change the thesis:
                - Upcoming earnings dates
                - Key policy decisions affecting the stock
                - Price levels (entry/exit considerations based on data, not advice)
                - Fundamental shifts that would invalidate the thesis

                ### 4. Portfolio Scorecard Summary
                | Ticker | Type | Weight | Expected Role | Key Risk |

                ### 5. Disclaimer
                This is educational research, not financial advice. Consult a qualified financial
                advisor before making investment decisions. Past performance does not guarantee future results.
                """.formatted(
                profile.getCountry(), profile.getInvestmentAmount(),
                profile.getRiskTolerance(), profile.getTimelineYears(),
                profile.getGoal(), profile.getAccounts(), profile.getCurrentHoldings(),
                stockAnalysis, macroContext,
                profile.getRiskTolerance(), profile.getInvestmentAmount()
        );

        String synthesis = claudeService.analyze(systemPrompt, userContent);

        return PhaseResult.builder()
                .phaseName(getPhaseName())
                .searchQueries(List.of())
                .rawResults(List.of())
                .synthesis(synthesis)
                .completedAt(LocalDateTime.now())
                .success(true)
                .build();
    }
}
