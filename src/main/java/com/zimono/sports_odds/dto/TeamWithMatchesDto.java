package com.zimono.sports_odds.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;

import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeamWithMatchesDto(
        String name,
        String description,
        Sport sport,
        Set<MatchRequest> homeMatches,
        Set<MatchRequest> awayMatches) {

    public static TeamWithMatchesDto toDto(Team team) {
        return new TeamWithMatchesDto(
                team.getName(),
                team.getDescription(),
                Sport.getByCode(team.getSport()),
                team.getHomeMatches().stream()
                        .map(MatchRequest::toDtoNoOdds).collect(Collectors.toSet()),
                team.getAwayMatches().stream()
                        .map(MatchRequest::toDtoNoOdds).collect(Collectors.toSet()));
    }

}
