package com.zimono.sports_odds.entity;

import java.util.Arrays;
import java.util.List;

public enum Sport {
    FOOTBALL(1, List.of(MatchOddSpecifier.ONE, MatchOddSpecifier.TWO, MatchOddSpecifier.DRAW)),
    BASKETBALL(2, List.of(MatchOddSpecifier.ONE, MatchOddSpecifier.TWO));

    private final int code;
    private final List<MatchOddSpecifier> specifiers;

    private Sport(int code, List<MatchOddSpecifier> specifiers) {
        this.code = code;
        this.specifiers = specifiers;
    }

    public int getCode() {
        return code;
    }

    public List<MatchOddSpecifier> getSpecifiers() {
        return specifiers;
    }


    public static String getNameByCode(int code) {
        return Arrays.stream(Sport.values())
                .filter(s -> s.code == code)
                .findFirst()
                .map(Sport::name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sport code: " + code));

    }

    public static Sport getByCode(int code) {
        return Arrays.stream(Sport.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid sport code: " + code));
    }


}
