CREATE TABLE demons
(
    id                 UUID PRIMARY KEY,
    created_at         TIMESTAMP    NOT NULL,
    deleted_at         TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL,
    creator_id         VARCHAR(255) NOT NULL CHECK (creator_id <> ''),
    creator_name       VARCHAR(255) NOT NULL CHECK (creator_name <> ''),
    description        TEXT         NOT NULL CHECK (description <> ''),
    difficulty         VARCHAR(255) NOT NULL,
    level_id           VARCHAR(255) NOT NULL CHECK (level_id <> ''),
    level_password     VARCHAR(255) NOT NULL CHECK (level_password <> ''),
    music_creator_name VARCHAR(255) NOT NULL CHECK (music_creator_name <> ''),
    music_id           VARCHAR(255) NOT NULL CHECK (music_id <> ''),
    music_name         VARCHAR(255) NOT NULL CHECK (music_name <> ''),
    music_url          VARCHAR(255) NOT NULL CHECK (music_url <> ''),
    position           INT          NOT NULL,
    points             INT          NOT NULL,
    CONSTRAINT demons_difficulty_check CHECK (((difficulty)::text = ANY ((ARRAY['EASY':: character varying, 'MEDIUM':: character varying, 'HARD':: character varying, 'INSANE':: character varying, 'EXTREME':: character varying])::text[])
) )
);