-- Create seq for Team table
CREATE SEQUENCE IF NOT EXISTS team_id_seq START WITH 1 INCREMENT BY 1;

-- Create a Team table
CREATE TABLE IF NOT EXISTS team (
    id BIGINT PRIMARY KEY DEFAULT nextval('team_id_seq'),
    "name" VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    sport INT NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp DEFAULT NULL
);

-- Create seq for Match table
CREATE SEQUENCE IF NOT EXISTS match_id_seq START WITH 1 INCREMENT BY 1;

-- Create a Match table
CREATE TABLE IF NOT EXISTS match (
    id BIGINT PRIMARY KEY DEFAULT nextval('match_id_seq'),
    description VARCHAR(255) NOT NULL,
    match_date date NOT NULL,
    match_time time NOT NULL,
    team_a_id BIGINT NOT NULL,
    team_b_id BIGINT NOT NULL,
    sport INT NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp DEFAULT NULL,
    CONSTRAINT fk_Match_Team_a FOREIGN KEY (team_a_id) REFERENCES team(id) ON DELETE CASCADE,
    CONSTRAINT fk_Match_Team_b FOREIGN KEY (team_b_id) REFERENCES team(id) ON DELETE CASCADE
);

-- Create seq for MatchOdds table
CREATE SEQUENCE IF NOT EXISTS matchodds_definitions_id_seq START WITH 1 INCREMENT BY 1;

-- Create a MatchOdds table with a foreign key to match
CREATE TABLE IF NOT EXISTS matchodds (
    id BIGINT PRIMARY KEY DEFAULT nextval('matchodds_definitions_id_seq'),
    match_id BIGINT NOT NULL,
    specifier VARCHAR(1) NOT NULL,
    odd DECIMAL(10, 2) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp DEFAULT NULL,
    UNIQUE (match_id, specifier),
    CONSTRAINT fk_MatchOdds_Match FOREIGN KEY (match_id) REFERENCES match(id) ON DELETE CASCADE
);

-- Creates index for fast lookups
-- CREATE INDEX IF NOT EXISTS idx_attribute_definitions_category ON attribute_definitions(xacml_category_id);