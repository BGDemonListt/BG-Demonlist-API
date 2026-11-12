CREATE TABLE player_claim_requests (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    requester_id UUID NOT NULL,
    player_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    reviewed_by_id UUID,
    reviewed_at TIMESTAMP,
    CONSTRAINT fk_player_claim_request_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_player_claim_request_player FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT fk_player_claim_request_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users (id)
);

CREATE INDEX idx_player_claim_status_created ON player_claim_requests (status, created_at);
CREATE INDEX idx_player_claim_requester ON player_claim_requests (requester_id, status);
