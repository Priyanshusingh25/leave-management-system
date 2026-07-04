package com.leaveflow.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leaveflow.backend.entity.Leave;
import com.leaveflow.backend.enums.LeaveStatus;
import com.leaveflow.backend.enums.LeaveType;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    Page<Leave> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:type IS NULL OR l.leaveType = :type)")
    Page<Leave> findByEmployeeIdWithFilters(@Param("employeeId") Long employeeId,
                                             @Param("status") LeaveStatus status,
                                             @Param("type") LeaveType type,
                                             Pageable pageable);

    @Query("SELECT l FROM Leave l WHERE l.status = :status " +
           "AND (:managerId IS NULL OR l.employee.manager.id = :managerId)")
    Page<Leave> findByStatusForManager(@Param("status") LeaveStatus status,
                                        @Param("managerId") Long managerId,
                                        Pageable pageable);

    @Query("SELECT l FROM Leave l WHERE l.employee.manager.id = :managerId")
    Page<Leave> findAllForManagerTeam(@Param("managerId") Long managerId, Pageable pageable);

    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    long countByEmployeeId(Long employeeId);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.employee.manager.id = :managerId AND l.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") LeaveStatus status);
}
