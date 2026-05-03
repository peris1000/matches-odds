package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.service.MatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    @Test
    void getMatchById_shouldReturnMatch() {
        MatchRequest dto = new MatchRequest(1L, 1L, 2L, "Desc", LocalDateTime.now(), Sport.FOOTBALL, Set.of());
        when(matchService.getMatchById(1L)).thenReturn(dto);

        ResponseEntity<MatchRequest> response = matchController.getMatchById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(matchService).getMatchById(1L);
    }
}
