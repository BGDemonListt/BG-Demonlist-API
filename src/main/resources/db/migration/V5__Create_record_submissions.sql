CREATE TABLE record_submissions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    progress INTEGER NOT NULL CHECK (progress BETWEEN 0 AND 100),
    youtube_url VARCHAR(255) NOT NULL CHECK (BTRIM(youtube_url) <> ''),
    raw_footage_url VARCHAR(255) NOT NULL CHECK (BTRIM(raw_footage_url) <> ''),
    description TEXT NOT NULL CHECK (BTRIM(description) <> ''),
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACCEPTED', 'PENDING', 'REJECTED')),
    demon_id UUID NOT NULL,
    holder_id UUID NOT NULL,
    CONSTRAINT fk_records_demon FOREIGN KEY (demon_id) REFERENCES demons (id),
    CONSTRAINT fk_records_holder FOREIGN KEY (holder_id) REFERENCES players (id)
);

CREATE INDEX idx_records_holder_status ON record_submissions (holder_id, status, deleted_at);
CREATE INDEX idx_records_demon_status ON record_submissions (demon_id, status, deleted_at);
