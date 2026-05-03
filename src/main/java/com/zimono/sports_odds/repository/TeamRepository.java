package com.zimono.sports_odds.repository;

import com.zimono.sports_odds.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"homeMatches", "awayMatches"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Team> findFullById(long id);

    @Query("""
    SELECT DISTINCT t
    FROM Team t
    LEFT JOIN FETCH t.homeMatches
    LEFT JOIN FETCH t.awayMatches
    WHERE t.sport = :sport
    """)
    Page<Team> findAllBy(Pageable pageable, Integer sport);
}
