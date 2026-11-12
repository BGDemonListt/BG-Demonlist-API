CREATE TABLE exceptions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    status_code INTEGER,
    exception_type VARCHAR(255),
    exception_message TEXT,
    stack_trace_string TEXT,
    method_name VARCHAR(255),
    class_name VARCHAR(255),
    line_number INTEGER,
    severity VARCHAR(255) CHECK (severity IN ('CRITICAL', 'INFORMATIONAL'))
);
