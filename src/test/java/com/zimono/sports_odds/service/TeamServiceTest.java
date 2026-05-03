package com.zimono.sports_odds.service;

import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.dto.TeamWithMatchesDto;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.exception.ResourceNotFoundException;
import com.zimono.sports_odds.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamService teamService;

    private Team team;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(1L);
        team.setName("Team A");
        team.setDescription("Description A");
        team.setSport(Sport.FOOTBALL.getCode());
    }

    @Test
    void getAllTeams_shouldReturnListOfTeamDtos() {
        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<TeamDto> result = teamService.getAllTeams();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Team A");
        verify(teamRepository).findAll();
    }

    @Test
    void getAllTeamsPaged_shouldReturnPageOfTeamDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Team> page = new PageImpl<>(List.of(team));
        when(teamRepository.findAllBy(pageable, Sport.FOOTBALL.getCode())).thenReturn(page);

        Page<TeamDto> result = teamService.getAllTeamsPaged(pageable, Sport.FOOTBALL.getCode());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Team A");
    }

    @Test
    void getAllTeamsPaged_withInvalidSport_shouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> teamService.getAllTeamsPaged(pageable, 999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTeamById_whenExists_shouldReturnTeamDto() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        TeamDto result = teamService.getTeamById(1L);

        assertThat(result.name()).isEqualTo("Team A");
    }

    @Test
    void getTeamById_whenNotExists_shouldThrowException() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTeamByIdFull_whenExists_shouldReturnTeamWithMatchesDto() {
        when(teamRepository.findFullById(1L)).thenReturn(Optional.of(team));

        TeamWithMatchesDto result = teamService.getTeamByIdFull(1L);

        assertThat(result.name()).isEqualTo("Team A");
    }

    @Test
    void createTeam_shouldSaveAndReturnTeamDto() {
        TeamDto inputDto = new TeamDto(null, "New Team", "New Desc", 1);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setId(2L);
            return savedTeam;
        });

        TeamDto result = teamService.createTeam(inputDto);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("New Team");
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_whenExists_shouldUpdateAndReturnTeamDto() {
        TeamDto updateDto = new TeamDto(1L, "Updated Team", "Updated Desc", 1);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        TeamDto result = teamService.updateTeam(1L, updateDto);

        assertThat(result.name()).isEqualTo("Updated Team");
        assertThat(team.getName()).isEqualTo("Updated Team");
        verify(teamRepository).save(team);
    }

    @Test
    void deleteTeam_whenExists_shouldDelete() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        teamService.deleteTeam(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    void deleteTeam_whenNotExists_shouldThrowException() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}