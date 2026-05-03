package com.zimono.sports_odds.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimono.sports_odds.entity.Team;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeamDto(
        Long id,
        String name,
        String description,
        Integer sport ) {

    public static Team toEntity(TeamDto dto) {
        Team entity = new Team();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setSport(dto.sport());
        return entity;
    }

}
