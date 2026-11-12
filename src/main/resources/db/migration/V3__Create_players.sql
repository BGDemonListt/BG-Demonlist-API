CREATE TABLE players (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    name VARCHAR(255) CHECK (name IS NULL OR CHAR_LENGTH(name) >= 2),
    region VARCHAR(64) CHECK (region IS NULL OR region IN (
        'BLAGOEVGRAD', 'BURGAS', 'DOBRICH', 'GABROVO', 'HASKOVO', 'KARDZHALI',
        'KYUSTENDIL', 'LOVECH', 'MONTANA', 'PAZARDZHIK', 'PERNIK', 'PLEVEN',
        'PLOVDIV', 'RAZGRAD', 'RUSE', 'SHUMEN', 'SILISTRA', 'SLIVEN', 'SMOLYAN',
        'SOFIA', 'STARA_ZAGORA', 'TARGOVISHTE', 'VARNA', 'VELIKO_TARNOVO', 'VIDIN',
        'VRATSA', 'YAMBOL'
    )),
    points DOUBLE PRECISION NOT NULL DEFAULT 0,
    rank INTEGER,
    hardest_demon_id UUID,
    CONSTRAINT fk_players_hardest_demon FOREIGN KEY (hardest_demon_id) REFERENCES demons (id)
);

CREATE INDEX idx_players_leaderboard_rank ON players (deleted_at, rank);
CREATE INDEX idx_players_leaderboard_points ON players (deleted_at, points);
