package com.zimono.sports_odds.service;

import com.zimono.sports_odds.dto.MatchOddDto;
import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.entity.Match;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.exception.ResourceNotFoundException;
import com.zimono.sports_odds.repository.MatchRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MatchService matchService;

    private Team teamA;
    private Team teamB;
    private Match match;
    private MatchRequest matchRequest;

    @BeforeEach
    void setUp() {
        teamA = new Team();
        teamA.setId(1L);
        teamA.setName("Team A");
        teamA.setSport(Sport.FOOTBALL.getCode());

        teamB = new Team();
        teamB.setId(2L);
        teamB.setName("Team B");
        teamB.setSport(Sport.FOOTBALL.getCode());

        match = new Match();
        match.setId(1L);
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        match.setDescription("Team A vs Team B");
        match.setMatchDate(LocalDate.now());
        match.setMatchTime(LocalTime.NOON);
        match.setSport(Sport.FOOTBALL.getCode());

        matchRequest = new MatchRequest(1L, 1L, 2L, "Team A vs Team B",
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON), Sport.FOOTBALL,
                Set.of(
                        new MatchOddDto("1", 1.5), new MatchOddDto("2", 2.5),
                        new MatchOddDto("X", 3.0))
                );
    }

    @Test
    void findAllMatchesPaged_shouldReturnPageOfMatchRequests() {
        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findAllFullPagedBy(pageable)).thenReturn(new PageImpl<>(List.of(match)));

        Page<MatchRequest> result = matchService.findAllMatchesPaged(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void getMatchById_whenExists_shouldReturnMatchRequest() {
        when(matchRepository.findFullById(1L)).thenReturn(Optional.of(match));

        MatchRequest result = matchService.getMatchById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getMatchById_whenNotExists_shouldThrowException() {
        when(matchRepository.findFullById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatchById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createMatch_shouldSaveAndReturnMatchRequest() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(teamB));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match savedMatch = invocation.getArgument(0);
            savedMatch.setId(1L);
            return savedMatch;
        });

        MatchRequest result = matchService.createMatch(matchRequest);

        assertThat(result.id()).isEqualTo(1L);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void updateMatch_whenExists_shouldUpdateAndReturnMatchRequest() {
        when(matchRepository.findFullById(1L)).thenReturn(Optional.of(match));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(teamB));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        MatchRequest result = matchService.updateMatch(1L, matchRequest);

        assertThat(result.id()).isEqualTo(1L);
        verify(matchRepository).save(match);
    }

    @Test
    void deleteMatch_whenExists_shouldDelete() {
        matchService.deleteMatch(1L);

        verify(matchRepository).deleteById(1L);
    }

    @Test
    void validateMatch_withInvalidTeams_shouldThrowException() {
        teamB.setSport(Sport.BASKETBALL.getCode());
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(teamB));

        assertThatThrownBy(() -> matchService.createMatch(matchRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Both teams should be on the sport requested.");
    }

    @Test
    void validateMatch_withDuplicateSpecifier_shouldThrowException() {
        MatchRequest invalidRequest = new MatchRequest(1L, 1L, 2L, "Desc",
                LocalDateTime.now(), Sport.FOOTBALL,
                Set.of(new MatchOddDto("1", 1.5), new MatchOddDto("1", 2.0))
        );
        
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(teamB));

        assertThatThrownBy(() -> matchService.createMatch(invalidRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("The match odd specifiers should be unique.");
    }

    @Test
    void updateMatch_whenNotExists_shouldThrowException() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(teamB));
        when(matchRepository.findFullById(1L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> matchService.updateMatch(1L, matchRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
