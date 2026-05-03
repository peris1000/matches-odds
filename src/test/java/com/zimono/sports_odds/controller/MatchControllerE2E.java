package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.TestcontainersConfiguration;
import com.zimono.sports_odds.dto.MatchOddDto;
import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.repository.MatchRepository;
import com.zimono.sports_odds.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.security.enabled=false")
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class MatchControllerE2E {

    @Autowired
    private TestRestTemplate restTemplate;

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
                "E2E Test Match",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of(new MatchOddDto("1", 1.5), new MatchOddDto("X", 3.0))
        );

        // When: Create Match
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MatchRequest> createEntity = new HttpEntity<>(request, headers);
        ResponseEntity<MatchRequest> createResponse = restTemplate.postForEntity("/api/matches", createEntity, MatchRequest.class);

        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        MatchRequest createdMatch = createResponse.getBody();
        assertThat(createdMatch).isNotNull();
        assertThat(createdMatch.id()).isNotNull();
        assertThat(createdMatch.description()).isEqualTo("E2E Test Match");

        // When: Get Match by ID
        ResponseEntity<MatchRequest> getResponse = restTemplate.getForEntity("/api/matches/" + createdMatch.id(), MatchRequest.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        MatchRequest retrievedMatch = getResponse.getBody();
        assertThat(retrievedMatch).isNotNull();
        assertThat(retrievedMatch.id()).isEqualTo(createdMatch.id());
        assertThat(retrievedMatch.odds()).hasSize(2);
    }

    @Test
    void shouldUpdateMatch() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MatchRequest createRequest = new MatchRequest(
                null,
                teamAId,
                teamBId,
                "Old Description",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of(new MatchOddDto("1", 1.5))
        );
        ResponseEntity<MatchRequest> createResponse = restTemplate.postForEntity("/api/matches", new HttpEntity<>(createRequest, headers), MatchRequest.class);
        Long matchId = createResponse.getBody().id();

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
        ResponseEntity<MatchRequest> updateResponse = restTemplate.exchange(
                "/api/matches/" + matchId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                MatchRequest.class
        );

        // Then
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        MatchRequest updatedMatch = updateResponse.getBody();
        assertThat(updatedMatch.description()).isEqualTo("New Description");
        assertThat(updatedMatch.odds()).hasSize(1);
        assertThat(updatedMatch.odds().iterator().next().specifier()).isEqualTo("2");
    }

    @Test
    void shouldDeleteMatch() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MatchRequest request = new MatchRequest(
                null,
                teamAId,
                teamBId,
                "To be deleted",
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS),
                Sport.FOOTBALL,
                Set.of()
        );
        ResponseEntity<MatchRequest> createResponse = restTemplate.postForEntity("/api/matches", new HttpEntity<>(request, headers), MatchRequest.class);
        Long matchId = createResponse.getBody().id();

        // When
        restTemplate.delete("/api/matches/" + matchId);

        // Then
        assertThat(matchRepository.findById(matchId)).isEmpty();
    }

    @Test
    void shouldReturnAllMatchesPaged() {
        // Given: Create 3 matches
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
            restTemplate.postForLocation("/api/matches", new HttpEntity<>(request, headers));
        }

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/matches?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(((java.util.List<?>) page.get("content"))).hasSize(3);
    }

    @Test
    void shouldReturnMatchesSearch() {
        // Given: Create 3 matches
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
            restTemplate.postForLocation("/api/matches", new HttpEntity<>(request, headers));
        }

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/matches/search?page=0&size=10&teamA=" + teamAId + "&teamB=" + teamBId + "&matchAfter=" + LocalDateTime.now().toLocalDate(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(((java.util.List<?>) page.get("content"))).hasSize(3);
    }
}
