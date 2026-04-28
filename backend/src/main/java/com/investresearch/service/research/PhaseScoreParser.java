package com.investresearch.service.research;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PhaseScoreParser {

    private static final String START = "---SCORES---";
    private static final String END   = "---END SCORES---";

    public record ParsedPhase(Map<String, String> scores, String synthesis) {}

    public static ParsedPhase parse(String raw) {
        if (raw == null) return new ParsedPhase(Map.of(), "");

        int si = raw.indexOf(START);
        int ei = raw.indexOf(END);

        if (si < 0 || ei < 0 || ei <= si) {
            return new ParsedPhase(Map.of(), raw.trim());
        }

        String block = raw.substring(si + START.length(), ei).trim();
        String text  = raw.substring(ei + END.length()).trim();

        Map<String, String> scores = new LinkedHashMap<>();
        for (String line : block.split("\n")) {
            line = line.trim();
            int eq = line.indexOf('=');
            if (eq > 0) {
                scores.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
            }
        }

        return new ParsedPhase(scores, text);
    }

    private PhaseScoreParser() {}
}
