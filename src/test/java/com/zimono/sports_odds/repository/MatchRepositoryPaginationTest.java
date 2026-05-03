package com.zimono.sports_odds.repository;

import com.zimono.sports_odds.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class MatchRepositoryPaginationTest {

    @Autowired
    private MatchRepository matchRepository;

    @Test
    void testFindAllFullPagedBy() {
        matchRepository.findAllFullPagedBy(PageRequest.of(0, 10));
    }
}
