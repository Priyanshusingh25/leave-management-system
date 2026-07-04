-- ==========================================================
-- LeaveFlow: Seed data
-- All demo accounts use password: Password@123
-- ==========================================================

-- Manager (no manager_id, self-referential root)
INSERT INTO employees (name, email, password_hash, department, role, manager_id)
VALUES ('Priya Sharma', 'manager@leaveflow.com', '$2b$10$YRTMbsqZUSvZtr/ntINmW.B9XlkOOad.Cb5Z1KMLDJvqf3vzQs5r6', 'Engineering', 'MANAGER', NULL);

-- Employees reporting to the manager (id=1)
INSERT INTO employees (name, email, password_hash, department, role, manager_id)
VALUES
    ('Arjun Mehta',   'employee@leaveflow.com', '$2b$10$1W/yE/F6R1Qj4Mb7e5cVa.FJjR4W2eOVvwhSBfV9tTetOQkcLbNyq', 'Engineering', 'EMPLOYEE', 1),
    ('Sara Iqbal',    'sara@leaveflow.com',     '$2b$10$pHkg2QkpjGJlPLhj6ps10OA/cqt/1EDVLsJa4oEiN9fbm0ALcR8vu', 'Engineering', 'EMPLOYEE', 1);

-- Sample leave requests
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, reason, status, manager_comments, reviewed_by)
VALUES
    (2, 'SICK',   CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '9 days', 'Fever and flu symptoms', 'APPROVED', 'Get well soon.', 1),
    (2, 'ANNUAL', CURRENT_DATE + INTERVAL '5 days',  CURRENT_DATE + INTERVAL '9 days', 'Family vacation', 'PENDING', NULL, NULL),
    (3, 'CASUAL', CURRENT_DATE - INTERVAL '3 days',  CURRENT_DATE - INTERVAL '3 days', 'Personal errand', 'REJECTED', 'Insufficient notice given for this date.', 1),
    (3, 'ANNUAL', CURRENT_DATE + INTERVAL '15 days', CURRENT_DATE + INTERVAL '20 days', 'Wedding trip', 'PENDING', NULL, NULL);