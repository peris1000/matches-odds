package com.zimono.sports_odds.entity;

import java.util.Arrays;

public enum MatchOddSpecifier {
    ONE("1"),
    TWO("2"),
    DRAW("X");

    public final String label;

    private MatchOddSpecifier(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static MatchOddSpecifier getByLabel(String label) {
        return Arrays.stream(MatchOddSpecifier.values())
                .filter(s -> s.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid odd specifier: " + label));
    }
}
