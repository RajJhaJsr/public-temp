DROP TABLE IF EXISTS smoke_test;

CREATE TABLE IF NOT EXISTS smoke_test (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO smoke_test (name, description, status) VALUES
('Test Record 1', 'First test record for smoke testing', 'ACTIVE'),
('Test Record 2', 'Second test record for smoke testing', 'ACTIVE'),
('Test Record 3', 'Third test record for smoke testing', 'INACTIVE');