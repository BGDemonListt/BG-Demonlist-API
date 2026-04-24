CREATE TABLE skillset_tags
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL,
    name       VARCHAR(50)  NOT NULL CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX uk_skillset_tags_name_active
    ON skillset_tags (LOWER(BTRIM(name)))
    WHERE deleted_at IS NULL;

CREATE TABLE demon_skillset_tags
(
    demon_id        UUID NOT NULL,
    skillset_tag_id UUID NOT NULL,
    PRIMARY KEY (demon_id, skillset_tag_id),
    CONSTRAINT fk_demon_skillset_tags_demon
        FOREIGN KEY (demon_id) REFERENCES demons (id),
    CONSTRAINT fk_demon_skillset_tags_skillset_tag
        FOREIGN KEY (skillset_tag_id) REFERENCES skillset_tags (id)
);

CREATE INDEX idx_demon_skillset_tags_skillset_tag_id
    ON demon_skillset_tags (skillset_tag_id);
