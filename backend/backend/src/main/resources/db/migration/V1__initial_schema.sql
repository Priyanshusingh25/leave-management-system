-- ==========================================================
-- LeaveFlow: Initial schema
-- ==========================================================

CREATE TABLE employees (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)        NOT NULL,
    email           VARCHAR(150)        NOT NULL UNIQUE,
    password_hash   VARCHAR(255)        NOT NULL,
    department      VARCHAR(100)        NOT NULL,
    role            VARCHAR(20)         NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER')),
    manager_id      BIGINT              REFERENCES employees(id) ON DELETE SET NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE TABLE leaves (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type          VARCHAR(30)     NOT NULL CHECK (leave_type IN ('SICK', 'CASUAL', 'ANNUAL', 'UNPAID', 'MATERNITY', 'PATERNITY')),
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    reason              VARCHAR(1000)   NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    manager_comments    VARCHAR(1000),
    reviewed_by         BIGINT          REFERENCES employees(id) ON DELETE SET NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
);

-- Indexes for common query patterns
CREATE INDEX idx_employees_email       ON employees(email);
CREATE INDEX idx_employees_manager_id  ON employees(manager_id);
CREATE INDEX idx_leaves_employee_id    ON leaves(employee_id);
CREATE INDEX idx_leaves_status         ON leaves(status);
CREATE INDEX idx_leaves_leave_type     ON leaves(leave_type);
CREATE INDEX idx_leaves_start_date     ON leaves(start_date);