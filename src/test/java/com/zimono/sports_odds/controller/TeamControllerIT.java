package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.TestcontainersConfiguration;
import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.dto.TeamWithMatchesDto;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.exception.ResourceNotFoundException;
import com.zimono.sports_odds.repository.TeamRepository;
import com.zimono.sports_odds.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class TeamControllerIT {

    @Autowired
    private TeamController teamController;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveTeam() {
        // Given
        TeamDto request = new TeamDto(null, "Test Team", "Description", Sport.FOOTBALL.getCode());
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        ResponseEntity<TeamDto> createResponse = teamController.createTeam(request, httpRequest);

        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TeamDto createdTeam = createResponse.getBody();
        assertThat(createdTeam.id()).isNotNull();
        assertThat(createdTeam.name()).isEqualTo("Test Team");

        ResponseEntity<TeamDto> getResponse = teamController.getTeamsById(createdTeam.id());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().name()).isEqualTo("Test Team");
    }

    @Test
    void shouldUpdateTeam() {
        // Given
        TeamDto request = new TeamDto(null, "Old Team", "Old Desc", Sport.FOOTBALL.getCode());
        TeamDto createdTeam = teamService.createTeam(request);

        TeamDto updateRequest = new TeamDto(createdTeam.id(), "New Team", "New Desc", Sport.BASKETBALL.getCode());

        // When
        ResponseEntity<TeamDto> updateResponse = teamController.updateTeam(createdTeam.id(), updateRequest);

        // Then
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().name()).isEqualTo("New Team");
    }

    @Test
    void shouldDeleteTeam() {
        // Given
        TeamDto request = new TeamDto(null, "Delete Me", "Desc", Sport.FOOTBALL.getCode());
        TeamDto createdTeam = teamService.createTeam(request);

        // When
        ResponseEntity<?> deleteResponse = teamController.deleteTeam(createdTeam.id());

        // Then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(teamRepository.findById(createdTeam.id())).isEmpty();
    }

    @Test
    void shouldGetAllTeams() {
        // Given
        teamService.createTeam(new TeamDto(null, "Team A", "Desc A", Sport.FOOTBALL.getCode()));
        teamService.createTeam(new TeamDto(null, "Team B", "Desc B", Sport.BASKETBALL.getCode()));

        // When
        ResponseEntity<List<TeamDto>> response = teamController.getAllTeams();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TeamDto> teams = response.getBody();
        assertThat(teams).hasSize(2);
        assertThat(teams).extracting(TeamDto::name).containsExactlyInAnyOrder("Team A", "Team B");
    }

    @Test
    void shouldGetAllTeamsPaged() {
        // Given
        teamService.createTeam(new TeamDto(null, "Team A", "Desc A", Sport.FOOTBALL.getCode()));
        teamService.createTeam(new TeamDto(null, "Team B", "Desc B", Sport.FOOTBALL.getCode()));
        teamService.createTeam(new TeamDto(null, "Team C", "Desc C", Sport.BASKETBALL.getCode()));

        // When
        ResponseEntity<Page<TeamDto>> response = teamController.getAllTeamsPaged(PageRequest.of(0, 2), Sport.FOOTBALL.getCode());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Page<TeamDto> page = response.getBody();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(TeamDto::name).containsExactlyInAnyOrder("Team A", "Team B");
    }

    @Test
    void shouldGetTeamWithMatches() {
        // Given
        TeamDto request = new TeamDto(null, "Team with Matches", "Desc", Sport.FOOTBALL.getCode());
        TeamDto createdTeam = teamService.createTeam(request);

        // When
        ResponseEntity<TeamWithMatchesDto> response = teamController.getTeamWithMatches(createdTeam.id());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TeamWithMatchesDto fullTeam = response.getBody();
        assertThat(fullTeam.name()).isEqualTo("Team with Matches");
        assertThat(fullTeam.homeMatches()).isEmpty();
        assertThat(fullTeam.awayMatches()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenTeamNotFound() {
        assertThatThrownBy(() -> teamController.getTeamsById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No team found with id: 999");
    }

    @Test
    void shouldThrowExceptionWhenCreatingInvalidTeam() {
        // Given
        TeamDto invalidRequest = new TeamDto(null, "", "Desc", Sport.FOOTBALL.getCode());
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When & Then
        assertThatThrownBy(() -> teamController.createTeam(invalidRequest, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidSport() {
        // Given
        TeamDto request = new TeamDto(null, "Team", "Desc", Sport.FOOTBALL.getCode());
        TeamDto createdTeam = teamService.createTeam(request);
        TeamDto invalidRequest = new TeamDto(createdTeam.id(), "Team", "Desc", 999);

        // When & Then
        assertThatThrownBy(() -> teamController.updateTeam(createdTeam.id(), invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sport is invalid");
    }
}
