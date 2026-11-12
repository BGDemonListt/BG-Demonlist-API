CREATE TABLE demons (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    level_title VARCHAR(255) NOT NULL CHECK (BTRIM(level_title) <> ''),
    level_id BIGINT NOT NULL CHECK (level_id >= 1),
    creator_name VARCHAR(255) NOT NULL CHECK (BTRIM(creator_name) <> ''),
    creator_id BIGINT NOT NULL CHECK (creator_id >= 1),
    description TEXT NOT NULL CHECK (BTRIM(description) <> ''),
    level_password VARCHAR(255) NOT NULL CHECK (BTRIM(level_password) <> ''),
    youtube_url VARCHAR(255),
    music_name VARCHAR(255) NOT NULL CHECK (BTRIM(music_name) <> ''),
    music_id BIGINT NOT NULL CHECK (music_id >= 1),
    music_creator_name VARCHAR(255) NOT NULL CHECK (BTRIM(music_creator_name) <> ''),
    music_url VARCHAR(255) NOT NULL CHECK (BTRIM(music_url) <> ''),
    requirement INTEGER NOT NULL,
    position INTEGER NOT NULL,
    points DOUBLE PRECISION NOT NULL,
    difficulty VARCHAR(255) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'INSANE', 'EXTREME'))
);

CREATE INDEX idx_demons_list_position ON demons (deleted_at, position);
