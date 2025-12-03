CREATE TABLE record_submissions
(
    id              UUID PRIMARY KEY,
    progress        INT          NOT NULL CHECK (progress >= 0 AND progress <= 100),
    youtube_url     VARCHAR(255) NOT NULL CHECK (youtube_url <> ''),
    raw_footage_url VARCHAR(255) NOT NULL CHECK (raw_footage_url <> ''),
    description     TEXT         NOT NULL CHECK (description <> ''),
    status          VARCHAR(50)  NOT NULL CHECK (((status)::text = ANY ((ARRAY['ACCEPTED':: character varying, 'PENDING':: character varying, 'REJECTED':: character varying])::text[]))),
    demon_id        UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_demon FOREIGN KEY (demon_id) REFERENCES demons (id),
    CONSTRAINT fk_records_user FOREIGN KEY (user_id) REFERENCES users (id)
);