package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.TestcontainersConfiguration;
import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.dto.TeamWithMatchesDto;
import com.zimono.sports_odds.entity.Sport;
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

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.security.enabled=false")
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class TeamControllerE2E {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveTeam() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TeamDto request = new TeamDto(null, "E2E Team", "Description", Sport.FOOTBALL.getCode());

        // When
        ResponseEntity<TeamDto> createResponse = restTemplate.postForEntity("/api/teams", new HttpEntity<>(request, headers), TeamDto.class);

        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TeamDto createdTeam = createResponse.getBody();
        assertThat(createdTeam).isNotNull();
        assertThat(createdTeam.id()).isNotNull();
        assertThat(createdTeam.name()).isEqualTo("E2E Team");

        ResponseEntity<TeamDto> getResponse = restTemplate.getForEntity("/api/teams/" + createdTeam.id(), TeamDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().name()).isEqualTo("E2E Team");
    }

    @Test
    void shouldUpdateTeam() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TeamDto request = new TeamDto(null, "Old Team", "Old Desc", Sport.FOOTBALL.getCode());
        ResponseEntity<TeamDto> createResponse = restTemplate.postForEntity("/api/teams", new HttpEntity<>(request, headers), TeamDto.class);
        TeamDto createdTeam = createResponse.getBody();
        assertThat(createdTeam).isNotNull();

        TeamDto updateRequest = new TeamDto(createdTeam.id(), "New Team", "New Desc", Sport.BASKETBALL.getCode());

        // When
        ResponseEntity<TeamDto> updateResponse = restTemplate.exchange(
                "/api/teams/" + createdTeam.id(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                TeamDto.class
        );

        // Then
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().name()).isEqualTo("New Team");
    }

    @Test
    void shouldDeleteTeam() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TeamDto request = new TeamDto(null, "Delete Me", "Desc", Sport.FOOTBALL.getCode());
        URI uri = restTemplate.postForLocation("/api/teams", new HttpEntity<>(request, headers));
        
        Long teamId = Long.valueOf(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));

        // When
        restTemplate.delete("/api/teams/" + teamId);

        // Then
        assertThat(teamRepository.findById(teamId)).isEmpty();
    }

    @Test
    void shouldGetAllTeams() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForLocation("/api/teams", new HttpEntity<>(new TeamDto(null, "Team A", "Desc A", Sport.FOOTBALL.getCode()), headers));
        restTemplate.postForLocation("/api/teams", new HttpEntity<>(new TeamDto(null, "Team B", "Desc B", Sport.BASKETBALL.getCode()), headers));

        // When
        ResponseEntity<List<TeamDto>> response = restTemplate.exchange(
                "/api/teams",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TeamDto>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TeamDto> teams = response.getBody();
        assertThat(teams).hasSize(2);
        assertThat(teams).extracting(TeamDto::name).containsExactlyInAnyOrder("Team A", "Team B");
    }

    @Test
    void shouldGetAllTeamsPaged() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/api/teams", new HttpEntity<>(new TeamDto(null, "Team A", "Desc A", Sport.FOOTBALL.getCode()), headers), TeamDto.class);
        restTemplate.postForEntity("/api/teams", new HttpEntity<>(new TeamDto(null, "Team B", "Desc B", Sport.FOOTBALL.getCode()), headers), TeamDto.class);
        restTemplate.postForEntity("/api/teams", new HttpEntity<>(new TeamDto(null, "Team C", "Desc C", Sport.BASKETBALL.getCode()), headers), TeamDto.class);


        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/teams/paged?page=0&size=2&sport=" + Sport.FOOTBALL.getCode(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(((java.util.List<?>) page.get("content"))).hasSize(2);
    }

    @Test
    void shouldGetTeamWithMatches() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TeamDto request = new TeamDto(null, "Team with Matches", "Desc", Sport.FOOTBALL.getCode());

        ResponseEntity<TeamDto> created =
                restTemplate.postForEntity("/api/teams", new HttpEntity<>(request, headers), TeamDto.class);

        Long teamId = created.getBody().id();

        // When
        ResponseEntity<TeamWithMatchesDto> response = restTemplate.getForEntity("/api/teams/" + teamId + "/full", TeamWithMatchesDto.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TeamWithMatchesDto fullTeam = response.getBody();
        assertThat(fullTeam).isNotNull();
        assertThat(fullTeam.name()).isEqualTo("Team with Matches");
    }
}
