ALTER TABLE users
    ADD COLUMN IF NOT EXISTS discord_username VARCHAR(255),
    ADD COLUMN IF NOT EXISTS discord_avatar_url VARCHAR(512),
    ADD COLUMN IF NOT EXISTS discord_linked_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_discord_id_key'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT users_discord_id_key UNIQUE (discord_id);
    END IF;
END $$;
