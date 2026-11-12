-- Development accounts; password for both users is mypassword123.
INSERT INTO users (
    id, created_at, updated_at, deleted_at, name, email, password, role, provider, enabled
)
VALUES
    ('00000000-0000-4000-8000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,
     'Admin', 'admin@example.com',
     '$2a$10$qTXZNzbiso8RnJe2FoP60.IruZDowWYQb6OcjgrCAwh2CSgWiXfK2', 'ADMIN', 'LOCAL', TRUE),
    ('00000000-0000-4000-8000-000000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,
     'Regular', 'user@example.com',
     '$2a$10$qTXZNzbiso8RnJe2FoP60.IruZDowWYQb6OcjgrCAwh2CSgWiXfK2', 'USER', 'LOCAL', TRUE);
