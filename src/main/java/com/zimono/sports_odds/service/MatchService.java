package com.zimono.sports_odds.service;

import com.querydsl.core.BooleanBuilder;
import com.zimono.sports_odds.annotation.LogExecutionTime;
import com.zimono.sports_odds.dto.MatchOddDto;
import com.zimono.sports_odds.dto.MatchRequest;
import com.zimono.sports_odds.entity.*;
import com.zimono.sports_odds.entity.QMatch;
import com.zimono.sports_odds.exception.ResourceNotFoundException;
import com.zimono.sports_odds.repository.MatchRepository;
import com.zimono.sports_odds.repository.TeamRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zimono.sports_odds.config.CacheConfig.*;

@Slf4j
@Service
@Transactional
public class MatchService {

    private MatchRepository repo;
    private TeamRepository teamRepo;

    public MatchService(MatchRepository repo, TeamRepository teamRepo) {
        this.repo = repo;
        this.teamRepo = teamRepo;
    }

    @Cacheable(value = MATCHES_CACHE, key = "#pageable.getPageNumber() + '-' + #pageable.getPageSize() + '-' + #pageable.getSort()")
    @Transactional(readOnly = true)
    @LogExecutionTime
    public Page<MatchRequest> findAllMatchesPaged(Pageable pageable) {
        Page<Match> list = repo.findAllFullPagedBy(pageable);
        return list.map(MatchRequest::toDtoWithOdds);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    @Cacheable(value = MATCHES_BY_DATE_AND_TEAMS_CACHE,
            key = "#pageable.getPageNumber() + '-' + #pageable.getPageSize() + '-' + #pageable.getSort() + '-' + #matchAfter + '-' + #matchBefore + '-' + #teamA + '-' + #teamB")
    public Page<MatchRequest> searchMatches(Pageable pageable,
                                            LocalDate matchAfter,
                                            LocalDate matchBefore,
                                            Long teamA,
                                            Long teamB) {

        QMatch match = QMatch.match;
        BooleanBuilder builder = new BooleanBuilder();

        // Add conditions only when parameters are present
        if (teamA != null && teamA > 0) {
            builder.and(match.teamA.id.eq(teamA));
        }
        if (teamB != null && teamB > 0) {
            builder.and(match.teamB.id.eq(teamB));
        }
        if (matchAfter != null) {
            builder.and(match.matchDate.goe(matchAfter));
        }
        if (matchBefore != null) {
            builder.and(match.matchDate.loe(matchBefore));
        }
        Page<Match> list = repo.findAllFullPaged3By(builder, pageable);
        return list.map(MatchRequest::toDtoWithOdds);
    }

    // Cache individual match lookups
    @Cacheable(value = MATCH_BY_ID_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public MatchRequest getMatchById(Long id) {
        log.info("Fetching match from database with ID: {}", id);
        return repo.findFullById(id)
                .map(MatchRequest::toDtoWithOdds)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    // Evict caches when creating matches
    @CacheEvict(value = {MATCHES_CACHE, MATCHES_BY_DATE_AND_TEAMS_CACHE}, allEntries = true)
    public MatchRequest createMatch(@NotNull MatchRequest dto) {
        log.info("Creating new match and evicting relevant caches");

        Pair<Team, Team> teamPair = getTeamTeamPair(dto);
        validateMatch(dto, teamPair);

        Match entity = new Match();
        updateProps(dto, entity, teamPair);
        entity.setId(null);
        repo.save(entity);
        return MatchRequest.toDtoWithOdds(entity);
    }

    @CacheEvict(value = {MATCHES_CACHE, MATCHES_BY_DATE_AND_TEAMS_CACHE}, allEntries = true)
    @Caching(evict = {
            @CacheEvict(value = MATCH_BY_ID_CACHE, key = "#id")
    })
    public MatchRequest updateMatch(Long id, @NotNull MatchRequest dto) {
        log.info("Updating match {} and evicting caches", id);

        Pair<Team, Team> teamPair = getTeamTeamPair(dto);
        validateMatch(dto, teamPair);

        return repo.findFullById(id).map(existing -> {
            updateProps(dto, existing, teamPair);
            repo.save(existing);
            return MatchRequest.toDtoWithOdds(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @CacheEvict(value = {MATCHES_CACHE, MATCH_BY_ID_CACHE, MATCHES_BY_DATE_AND_TEAMS_CACHE}, allEntries = true)
    @LogExecutionTime
    public void deleteMatch(Long id) {
        log.info("Deleting match {} and evicting all related caches", id);
        repo.deleteById(id);
    }

    private Pair<Team, Team> getTeamTeamPair(MatchRequest dto) {
        Team teamA = teamRepo.findById(dto.teamA())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + dto.teamA()));
        Team teamB = teamRepo.findById(dto.teamB())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + dto.teamB()));
        return Pair.of(teamA, teamB);
    }

    private static void updateProps(MatchRequest dto, Match entity, Pair<Team, Team> teamPair) {
        entity.setDescription(dto.description());
        entity.setMatchDate(dto.matchTime().toLocalDate());
        entity.setMatchTime(dto.matchTime().toLocalTime());
        entity.setTeamA(teamPair.getLeft());
        entity.setTeamB(teamPair.getRight());
        entity.setSport(dto.sport().getCode());
        syncOdds(dto, entity);
    }

    private static void syncOdds(MatchRequest dto, Match entity) {
        if (dto.odds() == null) {
            entity.getOdds().clear();
            return;
        }

        Map<String, MatchOdd> existingOddsMap = entity.getOdds().stream()
                .collect(Collectors.toMap(MatchOdd::getSpecifier, Function.identity()));

        Set<String> newSpecifiers = dto.odds().stream()
                .map(MatchOddDto::specifier)
                .collect(Collectors.toSet());

        // Remove odds not in DTO
        entity.getOdds().removeIf(odd -> !newSpecifiers.contains(odd.getSpecifier()));

        // Update or Add odds
        for (MatchOddDto oddDto : dto.odds()) {
            MatchOdd existing = existingOddsMap.get(oddDto.specifier());
            if (existing != null) {
                existing.setOdd(oddDto.odd());
            } else {
                entity.addOdd(new MatchOdd(entity, oddDto.specifier(), oddDto.odd()));
            }
        }
    }

    private void validateMatch(MatchRequest dto, Pair<Team, Team> teamPair) {
        // the 2 teams should be different
        if (dto.teamA().equals(dto.teamB())) {
            throw new IllegalStateException("The 2 teams should be different.");
        }

        // teams sport should be the same as the requested match's sport
        if (!teamPair.getLeft().getSport().equals(dto.sport().getCode()) ||
                !teamPair.getRight().getSport().equals(dto.sport().getCode())) {
            throw new IllegalStateException("Both teams should be on the sport requested.");
        }

        // each odd should be valid for sport's requested specifiers
        dto.odds().stream()
                .filter(odd -> !dto.sport().getSpecifiers().contains(MatchOddSpecifier.getByLabel(odd.specifier())))
                .findFirst()
                .ifPresent(odd -> {
                    throw new IllegalStateException("Sport requested does not support the odd specifier: " + odd.specifier());
                });

        // odd specifiers should be unique
        if (dto.odds().stream()
                .map(MatchOddDto::specifier)
                .distinct()
                .count() != dto.odds().size()) {
            throw new IllegalStateException("The match odd specifiers should be unique.");
        }

    }

}
