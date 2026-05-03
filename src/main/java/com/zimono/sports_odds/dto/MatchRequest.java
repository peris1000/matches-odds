package com.zimono.sports_odds.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimono.sports_odds.entity.Match;
import com.zimono.sports_odds.entity.Sport;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchRequest(
        Long id,
        Long teamA,
        Long teamB,
        String description,
        LocalDateTime matchTime,
        Sport sport,
        Set<MatchOddDto> odds) {

    public MatchRequest {
        if (teamA == null) {
            throw new IllegalArgumentException("teamA cannot be null");
        }
        if (teamB == null) {
            throw new IllegalArgumentException("teamB cannot be null");
        }
        if (teamA.equals(teamB)) {
            throw new IllegalArgumentException("Teams cannot be the same");
        }
        if (description == null || description.isBlank()) {
            description = teamA + " vs " + teamB;
        }
    }


    /**
     * Creates MatchRequest from Match entity without odds
     * @param match
     * @return
     */
    public static MatchRequest toDtoNoOdds(Match match) {
        return new MatchRequest(
                match.getId(),
                match.getTeamA().getId(),
                match.getTeamB().getId(),
                match.getDescription(),
                LocalDateTime.of(match.getMatchDate(), match.getMatchTime()),
                Sport.getByCode(match.getSport()),
                null
        );
    }

    /**
     * Creates MatchRequest from Match entity with odds
     * @param match
     * @return
     */
    public static MatchRequest toDtoWithOdds(Match match) {
        return new MatchRequest(
                match.getId(),
                match.getTeamA().getId(),
                match.getTeamB().getId(),
                match.getDescription(),
                LocalDateTime.of(match.getMatchDate(), match.getMatchTime()),
                Sport.getByCode(match.getSport()),
                match.getOdds()
                        .stream()
                        .map(o -> new MatchOddDto(o.getSpecifier(), o.getOdd()))
                        .collect(Collectors.toSet())
        );
    }

}
