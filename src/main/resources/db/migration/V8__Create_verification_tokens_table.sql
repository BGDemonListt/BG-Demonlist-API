-- Create table for VerificationToken entity
CREATE TABLE verification_tokens
(
    id          SERIAL PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL,
    token       VARCHAR(255),
    user_id     UUID      NOT NULL,
    expiry_date TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);
