-- Insert sample users
-- Password is: mypassword123 (for dev purposes)
INSERT INTO users (id, created_at, updated_at, deleted_at, name, score, email, password, role, provider, enabled)
VALUES (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 'Admin', 323.1, 'admin@example.com',
        '$2a$10$qTXZNzbiso8RnJe2FoP60.IruZDowWYQb6OcjgrCAwh2CSgWiXfK2', 'ADMIN', 'LOCAL', TRUE),
       (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 'Regular', 2, 'user@example.com',
        '$2a$10$qTXZNzbiso8RnJe2FoP60.IruZDowWYQb6OcjgrCAwh2CSgWiXfK2', 'USER', 'LOCAL', TRUE);
