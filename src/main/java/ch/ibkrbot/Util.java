package ch.ibkrbot;

import java.util.*;

public class Util {

    public static boolean isDouble(String str) {
        return Optional.ofNullable(str)
                .map(s -> s.matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) // Regex per double
                .orElse(false);
    }

    public static List<Integer> parseMarketRuleIds(String marketRuleIds) {
        Set<Integer> ruleIds = new HashSet<>();

        if (marketRuleIds == null || marketRuleIds.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String[] parts = marketRuleIds.split(",");
            for (String part : parts) {
                ruleIds.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Errore nel parsing MarketRuleIds: " + e.getMessage());
        }

        return new ArrayList<>(ruleIds);
    }
}
