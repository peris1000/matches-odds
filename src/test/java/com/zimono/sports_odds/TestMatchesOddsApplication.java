package com.zimono.sports_odds;

import org.springframework.boot.SpringApplication;

public class TestMatchesOddsApplication {

    public static void main(String[] args) {
        SpringApplication.from(MatchesOddsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
