package com.zimono.sports_odds.dto;

public record MatchOddDto(String specifier, Double odd) {

    public MatchOddDto {
        if (specifier == null || specifier.isBlank()) {
            throw new IllegalArgumentException("specifier cannot be null or empty");
        }
        if (odd == null) {
            throw new IllegalArgumentException("odd cannot be null");
        }
    }

}
