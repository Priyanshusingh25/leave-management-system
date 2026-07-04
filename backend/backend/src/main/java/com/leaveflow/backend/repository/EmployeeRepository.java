package com.leaveflow.backend.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leaveflow.backend.entity.Employee;

import java.util.Optional;
 
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
 
    Optional<Employee> findByEmail(String email);
 
    boolean existsByEmail(String email);
 
    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> search(@Param("search") String search, Pageable pageable);
 
    long countByManager_Id(Long managerId);
}
 

