CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    name VARCHAR(255) CHECK (name IS NULL OR CHAR_LENGTH(name) >= 2),
    email VARCHAR(255) NOT NULL UNIQUE CHECK (BTRIM(email) <> ''),
    password VARCHAR(255),
    role VARCHAR(255) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    provider VARCHAR(255) NOT NULL CHECK (provider IN ('LOCAL', 'GOOGLE')),
    enabled BOOLEAN NOT NULL,
    discord_id VARCHAR(255) UNIQUE,
    discord_username VARCHAR(255),
    discord_avatar_url VARCHAR(255),
    discord_linked_at TIMESTAMP,
    player_id UUID UNIQUE,
    CONSTRAINT fk_users_player FOREIGN KEY (player_id) REFERENCES players (id)
);
