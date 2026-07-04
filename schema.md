# LeaveFlow Database Schema Reference

Dedicated database schema reference for the LeaveFlow PostgreSQL implementation. I keep the schema here so the main README can stay focused on setup, architecture, and deployment.

## Overview
PostgreSQL 18+ relational database schema for employee leave management system.

## Entities

### 1. Employees Table
Stores employee information and role-based access.

```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER')),
    manager_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
```

**Constraints**:
- `email` is UNIQUE per employee
- `role` is constrained to EMPLOYEE or MANAGER
- `manager_id` is self-referential (optional, NULL for managers)

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE on `email`
- FOREIGN KEY on `manager_id`

---

### 2. Leaves Table
Stores leave request details and status.

```sql
CREATE TABLE leaves (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type VARCHAR(30) NOT NULL CHECK (leave_type IN ('SICK', 'CASUAL', 'ANNUAL', 'UNPAID', 'MATERNITY', 'PATERNITY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    manager_comments VARCHAR(1000),
    reviewed_by BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
);
```

**Constraints**:
- `leave_type` must be one of the defined types
- `status` defaults to PENDING and is constrained
- `end_date >= start_date` validation
- CASCADE delete on employee (orphan leave records deleted)
- `reviewed_by` tracks which manager approved/rejected

**Indexes**:
```sql
CREATE INDEX idx_leaves_employee_id ON leaves(employee_id);
CREATE INDEX idx_leaves_status ON leaves(status);
CREATE INDEX idx_leaves_leave_type ON leaves(leave_type);
CREATE INDEX idx_leaves_start_date ON leaves(start_date);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
```

---

## Relationships

### Employees → Employees (Manager Hierarchy)
```
employees.manager_id FOREIGN KEY → employees.id
```
- Allows building a manager-employee reporting structure
- NULL for managers with no manager
- Example: Arjun Mehta (employee_id=2) reports to Priya Sharma (manager_id=1)

### Leaves → Employees (Leave Owner)
```
leaves.employee_id FOREIGN KEY → employees.id
```
- Every leave belongs to exactly one employee
- ON DELETE CASCADE: deleting an employee deletes all their leave requests

### Leaves → Employees (Reviewed By)
```
leaves.reviewed_by FOREIGN KEY → employees.id
```
- Optional: NULL until leave is approved/rejected
- References the manager who reviewed the leave
- ON DELETE SET NULL: if manager deleted, approval info removed but leave persists

---

## Normalization

**Database is in 3NF (Third Normal Form)**:

1. **1NF (Atomic Values)**:
   - All columns contain single, atomic values
   - No repeating groups

2. **2NF (No Partial Dependencies)**:
   - Non-key attributes depend on entire primary key
   - No partial dependencies

3. **3NF (No Transitive Dependencies)**:
   - No transitive dependencies through non-key attributes
   - Employee department is directly stored (not a separate table for this MVP)

---

## Seed Data

Default test accounts loaded via `V2__seed_data.sql`:

```sql
-- Manager
INSERT INTO employees (name, email, password_hash, department, role, manager_id)
VALUES ('Priya Sharma', 'manager@leaveflow.com', '<bcrypt_hash>', 'Engineering', 'MANAGER', NULL);

-- Employees
INSERT INTO employees (name, email, password_hash, department, role, manager_id)
VALUES 
  ('Arjun Mehta', 'employee@leaveflow.com', '<bcrypt_hash>', 'Engineering', 'EMPLOYEE', 1),
  ('Sara Iqbal', 'sara@leaveflow.com', '<bcrypt_hash>', 'Engineering', 'EMPLOYEE', 1);

-- Sample Leave Requests
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, reason, status, manager_comments, reviewed_by)
VALUES 
  (2, 'SICK', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '9 days', 'Fever', 'APPROVED', NULL, 1),
  (2, 'ANNUAL', CURRENT_DATE + INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', 'Vacation', 'PENDING', NULL, NULL),
  (3, 'CASUAL', CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE - INTERVAL '3 days', 'Personal', 'REJECTED', 'Short notice', 1),
  (3, 'ANNUAL', CURRENT_DATE + INTERVAL '15 days', CURRENT_DATE + INTERVAL '20 days', 'Wedding trip', 'PENDING', NULL, NULL);
```

All passwords hashed with BCrypt (rounds=10). Default: `Password@123`

---

## Scalability Considerations

### Current Indexes
Optimized for:
- User authentication: `idx_employees_email`
- Manager queries: `idx_employees_manager_id`
- Leave filtering: `idx_leaves_employee_id`, `idx_leaves_status`, `idx_leaves_leave_type`
- Date-based searches: `idx_leaves_start_date`

### Future Optimizations
1. **Partitioning**: Partition `leaves` by year (PARTITION BY RANGE on start_date)
2. **Archive Table**: Move past leaves to archive table after 2 years
3. **Caching**: Redis for dashboard statistics (pending approvals count)
4. **Read Replicas**: Separate read-only replica for reporting
5. **Materialized Views**: Pre-aggregate leave statistics

### Performance Tips
- Use pagination (default 10 items/page) for list endpoints
- Pre-compute dashboard stats or cache results
- Consider adding `leaves(employee_id, status)` composite index for quick filtering

---

## Backup & Recovery

### PostgreSQL Backup
```bash
# Logical backup
pg_dump -U leaveflow_user leaveflow > leaveflow_backup.sql

# Restore
psql -U leaveflow_user leaveflow < leaveflow_backup.sql
```

### Docker Persistent Volume
```bash
# Data persists in named volume 'postgres_data'
docker-compose down  # Data preserved
docker-compose up    # Data restored
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-07-03 | Initial schema with employees, leaves, and relationships |

---

## SQL Utilities

### Get Manager Hierarchy
```sql
WITH RECURSIVE mgr_hierarchy AS (
  SELECT id, name, manager_id, 0 as level
  FROM employees
  WHERE manager_id IS NULL
  UNION ALL
  SELECT e.id, e.name, e.manager_id, h.level + 1
  FROM employees e
  INNER JOIN mgr_hierarchy h ON e.manager_id = h.id
)
SELECT id, name, level FROM mgr_hierarchy ORDER BY level, name;
```

### Get Pending Approvals for Manager
```sql
SELECT l.*, e.name as employee_name
FROM leaves l
INNER JOIN employees e ON l.employee_id = e.id
WHERE l.employee.manager_id = :manager_id AND l.status = 'PENDING'
ORDER BY l.created_at ASC;
```

### Get Employee Leave Statistics
```sql
SELECT 
  employee_id,
  COUNT(*) as total_requests,
  SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approved,
  SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending,
  SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected
FROM leaves
GROUP BY employee_id;
```

### Calculate Leave Days Used
```sql
SELECT 
  employee_id,
  leave_type,
  SUM(EXTRACT(DAY FROM end_date - start_date) + 1) as total_days
FROM leaves
WHERE status = 'APPROVED' AND EXTRACT(YEAR FROM start_date) = 2026
GROUP BY employee_id, leave_type;
```

---

**Database Created**: July 3-4, 2026  
**Last Modified**: July 4, 2026  
**PostgreSQL Version**: 18+
