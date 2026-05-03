package com.zimono.sports_odds.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match extends BaseEntity {

    @Column(name = "description", nullable = false)
    @NotBlank(message = "Description cannot be null or empty")
    private String description;

    @Column(name = "match_date", nullable = false)
    @NotNull(message = "Match date cannot be null")
    private LocalDate matchDate;

    @Column(name = "match_time", nullable = false)
    @NotNull(message = "Match time cannot be null")
    private LocalTime matchTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    @Column(name="sport", nullable = false)
    @NotNull(message = "Sport cannot be null")
    @Positive(message = "Sport must be positive")
    private Integer sport;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MatchOdd> odds = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match)) return false;
        return id != null && Objects.equals(id, match.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


    public void addOdd(MatchOdd odd) {
        odds.add(odd);
        odd.setMatch(this);
    }

    public void removeOdd(MatchOdd odd) {
        odds.remove(odd);
        odd.setMatch(null);
    }

}
