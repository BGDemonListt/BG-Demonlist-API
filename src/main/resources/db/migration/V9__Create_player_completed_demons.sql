CREATE TABLE player_completed_demons (
    player_id UUID NOT NULL,
    demon_id UUID NOT NULL,
    PRIMARY KEY (player_id, demon_id),
    CONSTRAINT fk_player_completed_demons_player FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT fk_player_completed_demons_demon FOREIGN KEY (demon_id) REFERENCES demons (id)
);

CREATE INDEX idx_player_completed_demons_demon_id ON player_completed_demons (demon_id);
