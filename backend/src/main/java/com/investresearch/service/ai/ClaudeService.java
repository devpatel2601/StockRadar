package com.investresearch.service.ai;

import com.investresearch.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final ChatModel chatModel;  // provider-agnostic — swap Groq ↔ Claude in pom.xml + application.properties

    public String analyze(String systemPrompt, String userContent) {
        log.debug("Calling AI — userContent length={}", userContent.length());
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userContent)
        ));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    public String synthesizePhase(String phaseName, String phaseInstructions, List<SearchResult> results) {
        String systemPrompt = """
                You are an expert investment research analyst specializing in Canadian markets.
                Synthesize the provided search results into structured, factual analysis.
                - Note the source URL and date of each piece of information.
                - Clearly distinguish between facts (earnings, prices) and opinions (analyst targets, forecasts).
                - If results contain mock/placeholder data, state that clearly and indicate what real data would show.
                - Be concise but thorough. Use headers and bullet points for readability.
                """;

        String searchBlock = results.stream()
                .map(r -> "Query: %s\nTitle: %s\nURL: %s\nContent: %s"
                        .formatted(r.getQuery(), r.getTitle(), r.getUrl(), r.getFullContent()))
                .collect(Collectors.joining("\n\n---\n\n"));

        String userContent = """
                Phase: %s

                Instructions:
                %s

                Search Results:
                %s
                """.formatted(phaseName, phaseInstructions, searchBlock);

        return analyze(systemPrompt, userContent);
    }
}
