package com.zimono.sports_odds.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "matchodds")
@Getter
@Setter
@NoArgsConstructor
public class MatchOdd extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnore
    private Match match;

    @Column(name = "specifier", nullable = false)
    private String specifier;

    @Column(name = "odd", nullable = false)
    @NotNull
    private double odd;

    public MatchOdd(Match match, String specifier, double odd) {
        this.match = match;
        this.specifier = specifier;
        this.odd = odd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchOdd that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
