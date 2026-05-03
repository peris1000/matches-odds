package com.zimono.sports_odds.controller;

import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.service.MatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService service;

    public MatchController(MatchService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<MatchRequest>> getAllMatchesPaged(
            @PageableDefault(size = 2, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.findAllMatchesPaged(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MatchRequest>> searchMatches(
            @PageableDefault(size = 2, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) LocalDate matchAfter,
            @RequestParam(required = false) LocalDate matchBefore,
            @RequestParam(required = false) Long teamA,
            @RequestParam(required = false) Long teamB) {
        return ResponseEntity.ok(service.searchMatches(pageable, matchAfter, matchBefore, teamA, teamB));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchRequest> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<MatchRequest> createMatch(@RequestBody MatchRequest dto, HttpServletRequest request) {
        MatchRequest matchRequest = service.createMatch(dto);
        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + matchRequest.id())).body(matchRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMatch(@PathVariable Long id, @RequestBody MatchRequest dto) {
        MatchRequest updated = service.updateMatch(id, dto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        service.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }


}
