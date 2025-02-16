package ch.ibkrbot;

import java.util.Optional;

public class Util {

    public static boolean isDouble(String str) {
        return Optional.ofNullable(str)
                .map(s -> s.matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) // Regex per double
                .orElse(false);
    }
}
