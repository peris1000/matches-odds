package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    @Test
    void getAllTeams_shouldReturnList() {
        TeamDto dto = new TeamDto(1L, "Team A", "Desc", Sport.FOOTBALL.getCode());
        when(teamService.getAllTeams()).thenReturn(List.of(dto));

        ResponseEntity<List<TeamDto>> response = teamController.getAllTeams();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(teamService).getAllTeams();
    }

    @Test
    void getTeamsById_shouldReturnTeam() {
        TeamDto dto = new TeamDto(1L, "Team A", "Desc", Sport.FOOTBALL.getCode());
        when(teamService.getTeamById(1L)).thenReturn(dto);

        ResponseEntity<TeamDto> response = teamController.getTeamsById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Team A");
    }
}
