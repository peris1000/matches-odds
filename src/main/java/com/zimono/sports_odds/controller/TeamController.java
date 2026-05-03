package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.dto.TeamWithMatchesDto;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        return new ResponseEntity<>(service.getAllTeams(), HttpStatus.OK);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TeamDto>> getAllTeamsPaged(
            @PageableDefault(size = 2, sort = "id") Pageable pageable,
            @RequestParam(required = false) Integer sport) {
        return new ResponseEntity<>(service.getAllTeamsPaged(pageable, sport), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeamsById(@PathVariable long id) {
        return new ResponseEntity<>(service.getTeamById(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<TeamWithMatchesDto> getTeamWithMatches(@PathVariable long id) {
        return new ResponseEntity<>(service.getTeamByIdFull(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@RequestBody TeamDto dto, HttpServletRequest request) {
        validateDto(dto);
        TeamDto created = service.createTeam(dto);
        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable long id, @RequestBody TeamDto dto) {
        validateDto(dto);
        TeamDto updated = service.updateTeam(id, dto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable long id) {
        service.deleteTeam(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public void validateDto(TeamDto dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (dto.sport() == null || dto.sport() <= 0) {
            throw new IllegalArgumentException("Sport should be a positive integer number");
        }
        if (Arrays.stream(Sport.values()).noneMatch(s -> s.getCode() == dto.sport())) {
            throw new IllegalArgumentException("Sport is invalid, valid values: " +
                    Arrays.stream(Sport.values()).map(Sport::getCode).toList());
        }
    }

}
