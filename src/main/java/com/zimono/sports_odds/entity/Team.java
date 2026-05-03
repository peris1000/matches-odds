package com.zimono.sports_odds.entity;

import com.zimono.sports_odds.dto.TeamDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Name cannot be null or empty")
    @Size(max = 255, message = "Name max length is 255 chars")
    private String name;

    @Column
    @Size(max = 255, message = "Description max length is 255 chars")
    private String description;

    @Column(name="sport", nullable = false)
    @NotNull(message = "Sport cannot be null")
    @Positive(message = "Sport must be positive")
    private Integer sport;

    @OneToMany(mappedBy = "teamA", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> homeMatches = new HashSet<>();

    @OneToMany(mappedBy = "teamB", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> awayMatches = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public TeamDto toDto() {
        return new TeamDto(id, name, description, sport);
    }

}
