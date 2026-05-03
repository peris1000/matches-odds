package com.zimono.sports_odds.service;

import com.zimono.sports_odds.annotation.LogExecutionTime;
import com.zimono.sports_odds.dto.TeamDto;
import com.zimono.sports_odds.dto.TeamWithMatchesDto;
import com.zimono.sports_odds.entity.Sport;
import com.zimono.sports_odds.entity.Team;
import com.zimono.sports_odds.exception.ResourceNotFoundException;
import com.zimono.sports_odds.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zimono.sports_odds.config.CacheConfig.*;

@Slf4j
@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @LogExecutionTime
    @Cacheable(value = TEAMS_CACHE)
    @Transactional(readOnly = true)
    public List<TeamDto> getAllTeams() {
        log.debug("Fetching all teams");
        return teamRepository.findAll()
                .stream()
                .map(Team::toDto)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    @Cacheable(value = TEAMS_CACHE, key = "#pageable.getPageNumber() + '-' + #pageable.getPageSize() + '-' + #pageable.getSort() + '-' + #sport")
    @Transactional(readOnly = true)
    public Page<TeamDto> getAllTeamsPaged(Pageable pageable, Integer sport) {
        log.debug("Fetching all teams paged");
        Sport sportType = sport != null ? Sport.getByCode(sport) : null;
        if (sportType == null) {
            throw new IllegalArgumentException("Invalid sport code: " + sport);
        }
        Page<Team> page = teamRepository.findAllBy(pageable, sport);
        return page.map(Team::toDto);
    }

    @Cacheable(value = TEAM_BY_ID_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public TeamDto getTeamById(long id) {
        return teamRepository.findById(id).map(Team::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
    }

    @LogExecutionTime
    @Cacheable(value = TEAM_FULL_BY_ID_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public TeamWithMatchesDto getTeamByIdFull(long id) {
        Team team = teamRepository.findFullById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return TeamWithMatchesDto.toDto(team);
    }

    @CacheEvict(value = {TEAMS_CACHE}, allEntries = true)
    public TeamDto createTeam(TeamDto dto) {
        log.info("Creating new team: {}", dto.name());
        Team entity = TeamDto.toEntity(dto);

        teamRepository.save(entity);
        return entity.toDto();
    }

    @CacheEvict(value = {TEAMS_CACHE}, allEntries = true)
    @Caching(evict = {
            @CacheEvict(value = TEAM_BY_ID_CACHE, key = "#id")
    })
    public TeamDto updateTeam(long id, TeamDto dto) {
        log.info("Updating team: {}", id);
        return teamRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.name());
                    existing.setDescription(dto.description());
                    teamRepository.save(existing);
                    return existing.toDto();
                })
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
    }

    @CacheEvict(value = {TEAMS_CACHE, TEAM_BY_ID_CACHE}, allEntries = true)
    public void deleteTeam(long id) {
        log.info("Deleting team: {}", id);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
        teamRepository.deleteById(team.getId());
    }


}
