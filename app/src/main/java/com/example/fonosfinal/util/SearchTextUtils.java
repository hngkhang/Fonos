package com.example.fonosfinal.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchTextUtils {

    private SearchTextUtils() {
    }

    public static String normalizeSearchText(String input) {
        if (input == null) {
            return "";
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('đ', 'd').replace('Đ', 'd');
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^\\p{Alnum}\\s]", " ");
        return normalized.replaceAll("\\s+", " ").trim();
    }
}
