-- Create table for User entity
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP,
    name       VARCHAR(255) CHECK (char_length(name) >= 2),
    surname    VARCHAR(255) CHECK (char_length(surname) >= 2),
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255),
    role       VARCHAR(255) NOT NULL,
    provider   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL,
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$'),
    CONSTRAINT users_email_not_blank CHECK (email IS NOT NULL AND email <> ''),
    CONSTRAINT users_provider_check CHECK (((provider)::text = ANY ((ARRAY['LOCAL'::character varying, 'GOOGLE'::character varying, 'FACEBOOK'::character varying])::text[]))),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);
