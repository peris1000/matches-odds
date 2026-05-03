package com.zimono.sports_odds.repository;

import com.querydsl.core.BooleanBuilder;
import com.zimono.sports_odds.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, QuerydslPredicateExecutor<Match> {

    @EntityGraph(attributePaths = {"odds"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Match> findFullById(Long id);

    @EntityGraph(attributePaths = {"odds", "teamA", "teamB"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("""
        SELECT m FROM Match m
    """)
    Page<Match> findAllFullPagedBy(Pageable pageable);

    @EntityGraph(attributePaths = {"odds"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Match> findAllFullPaged3By(BooleanBuilder builder, Pageable pageable);
}
