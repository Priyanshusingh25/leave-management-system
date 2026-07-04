package com.leaveflow.backend.controller;

import com.leaveflow.backend.dto.EmployeeDashboardResponse;
import com.leaveflow.backend.dto.EmployeeResponse;
import com.leaveflow.backend.dto.LeaveRequestDto;
import com.leaveflow.backend.dto.LeaveResponse;
import com.leaveflow.backend.dto.PageResponse;
import com.leaveflow.backend.service.EmployeeService;
import com.leaveflow.backend.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee-specific operations like leave management and profile")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;

    @GetMapping("/profile")
    @Operation(summary = "Get Current Employee Profile", description = "Retrieve the profile of the currently authenticated employee.")
    public ResponseEntity<EmployeeResponse> getCurrentProfile(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse response = employeeService.getCurrentEmployee(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Get Employee Profile by ID", description = "Retrieve the profile of an employee by their ID.")
    public ResponseEntity<EmployeeResponse> getEmployeeProfile(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leaves/apply")
    @Operation(summary = "Apply for Leave", description = "Submit a new leave request. Start date must be >= today and end date >= start date.")
    public ResponseEntity<LeaveResponse> applyLeave(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestDto request) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse emp = employeeService.getCurrentEmployee(email);
        
        LeaveResponse response = leaveService.applyLeave(emp.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/leaves/{leaveId}")
    @Operation(summary = "Update Pending Leave Request", description = "Modify a pending leave request. Only PENDING requests can be edited.")
    public ResponseEntity<LeaveResponse> updateLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveRequestDto request) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse emp = employeeService.getCurrentEmployee(email);
        
        LeaveResponse response = leaveService.updateLeave(emp.getId(), leaveId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/leaves/{leaveId}")
    @Operation(summary = "Cancel Pending Leave Request", description = "Cancel a pending leave request. Only PENDING requests can be cancelled.")
    public ResponseEntity<Void> cancelLeave(
            Authentication authentication,
            @PathVariable Long leaveId) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse emp = employeeService.getCurrentEmployee(email);
        
        leaveService.cancelLeave(emp.getId(), leaveId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/leaves/{leaveId}")
    @Operation(summary = "Get Leave Details", description = "Retrieve details of a specific leave request.")
    public ResponseEntity<LeaveResponse> getLeaveDetails(@PathVariable Long leaveId) {
        LeaveResponse response = leaveService.getLeaveById(leaveId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaves")
    @Operation(summary = "Get Leave History", description = "Retrieve leave history with optional filtering by status or leave type.")
    public ResponseEntity<PageResponse<LeaveResponse>> getLeaveHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType) {
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse emp = employeeService.getCurrentEmployee(email);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveResponse> responses = leaveService.getEmployeeLeaveHistory(emp.getId(), status, leaveType, pageable);
        
        PageResponse<LeaveResponse> pageResponse = PageResponse.from(responses);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Employee Dashboard", description = "Retrieve dashboard statistics: total/approved/pending/rejected requests and recent activity.")
    public ResponseEntity<EmployeeDashboardResponse> getEmployeeDashboard(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        EmployeeResponse emp = employeeService.getCurrentEmployee(email);
        
        long totalRequests = 0, approvedRequests = 0, pendingRequests = 0, rejectedRequests = 0;
        Pageable pageable = PageRequest.of(0, 100);
        Page<LeaveResponse> allLeaves = leaveService.getEmployeeLeaveHistory(emp.getId(), null, null, pageable);
        
        for (LeaveResponse leave : allLeaves) {
            totalRequests++;
            switch (leave.getStatus()) {
                case "APPROVED" -> approvedRequests++;
                case "PENDING" -> pendingRequests++;
                case "REJECTED" -> rejectedRequests++;
            }
        }
        
        // Recent activity (last 5)
        Pageable recentPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<LeaveResponse> recentActivity = leaveService.getEmployeeLeaveHistory(emp.getId(), null, null, recentPageable);
        
        EmployeeDashboardResponse response = EmployeeDashboardResponse.builder()
                .totalRequests(totalRequests)
                .approvedRequests(approvedRequests)
                .pendingRequests(pendingRequests)
                .rejectedRequests(rejectedRequests)
                .recentActivity(recentActivity.getContent())
                .build();
        
        return ResponseEntity.ok(response);
    }
}