package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.TestcontainersConfiguration;
import com.zimono.sports_odds.dto.MatchOddDto;
import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.repository.MatchRepository;
import com.zimono.sports_odds.repository.TeamRepository;
import com.zimono.sports_odds.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class MatchControllerIT {

    @Autowired
    private MatchService matchService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    private Long teamAId;
    private Long teamBId;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        teamRepository.deleteAll();

        Team teamA = new Team();
        teamA.setName("Team A");
        teamA.setSport(Sport.FOOTBALL.getCode());
        teamA = teamRepository.save(teamA);
        teamAId = teamA.getId();

        Team teamB = new Team();
        teamB.setName("Team B");
        teamB.setSport(Sport.FOOTBALL.getCode());
        teamB = teamRepository.save(teamB);
        teamBId = teamB.getId();
    }

    @Test
    void shouldCreateAndRetrieveMatch() {
        // Given
        MatchRequest request = new MatchRequest(
                null,
                teamAId,
                teamBId,
                "Integration Test Match",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of(new MatchOddDto("1", 1.5), new MatchOddDto("X", 3.0))
        );

        // When: Create Match
        MatchRequest createdMatch = matchService.createMatch(request);

        // Then
        assertThat(createdMatch.id()).isNotNull();
        assertThat(createdMatch.description()).isEqualTo("Integration Test Match");

        // When: Get Match by ID
        MatchRequest retrievedMatch = matchService.getMatchById(createdMatch.id());

        // Then
        assertThat(retrievedMatch).isNotNull();
        assertThat(retrievedMatch.id()).isEqualTo(createdMatch.id());
        assertThat(retrievedMatch.odds()).hasSize(2);
    }

    @Test
    void shouldUpdateMatch() {
        // Given
        MatchRequest createRequest = new MatchRequest(
                null,
                teamAId,
                teamBId,
                "Old Description",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of(new MatchOddDto("1", 1.5))
        );
        MatchRequest createdMatch = matchService.createMatch(createRequest);
        Long matchId = createdMatch.id();

        MatchRequest updateRequest = new MatchRequest(
                matchId,
                teamAId,
                teamBId,
                "New Description",
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of(new MatchOddDto("2", 2.0))
        );

        // When
        MatchRequest updatedMatch = matchService.updateMatch(matchId, updateRequest);

        // Then
        assertThat(updatedMatch.description()).isEqualTo("New Description");
        assertThat(updatedMatch.odds()).hasSize(1);
        assertThat(updatedMatch.odds().iterator().next().specifier()).isEqualTo("2");
    }

    @Test
    void shouldDeleteMatch() {
        // Given
        MatchRequest request = new MatchRequest(
                null,
                teamAId,
                teamBId,
                "To be deleted",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of()
        );
        MatchRequest createdMatch = matchService.createMatch(request);
        Long matchId = createdMatch.id();

        // When
        matchService.deleteMatch(matchId);

        // Then
        assertThat(matchRepository.findById(matchId)).isEmpty();
    }

    @Test
    void shouldReturnAllMatchesPaged() {
        // Given: Create 3 matches
        for (int i = 0; i < 3; i++) {
            MatchRequest request = new MatchRequest(
                    null,
                    teamAId,
                    teamBId,
                    "Match " + i,
                    LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                    Sport.FOOTBALL,
                    Set.of()
            );
            matchService.createMatch(request);
        }

        // When
        var result = matchService.findAllMatchesPaged(PageRequest.of(0, 2));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }
}
