package com.leaveflow.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leaveflow.backend.dto.LeaveResponse;
import com.leaveflow.backend.dto.ManagerDashboardResponse;
import com.leaveflow.backend.dto.PageResponse;
import com.leaveflow.backend.dto.ReviewLeaveRequest;
import com.leaveflow.backend.service.ManagerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager", description = "Manager-specific operations for leave approval and team management")
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Manager Dashboard", description = "Retrieve manager dashboard: total employees, pending approvals, counts, and recent activity.")
    public ResponseEntity<ManagerDashboardResponse> getManagerDashboard(Authentication authentication) {
        Long managerId = extractUserIdFromAuthentication(authentication);
        ManagerDashboardResponse response = managerService.getManagerDashboard(managerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-leaves")
    @Operation(summary = "Get Pending Leave Requests", description = "Retrieve all pending leave requests for employees under this manager, with pagination.")
    public ResponseEntity<PageResponse<LeaveResponse>> getPendingLeaves(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long managerId = extractUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<LeaveResponse> responses = managerService.getPendingLeaves(managerId, pageable);
        
        PageResponse<LeaveResponse> pageResponse = PageResponse.from(responses);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/team-leave-history")
    @Operation(summary = "Get Team Leave History", description = "Retrieve leave history for all employees under this manager.")
    public ResponseEntity<PageResponse<LeaveResponse>> getTeamLeaveHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long managerId = extractUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveResponse> responses = managerService.getTeamLeaveHistory(managerId, pageable);
        
        PageResponse<LeaveResponse> pageResponse = PageResponse.from(responses);
        return ResponseEntity.ok(pageResponse);
    }

    @PutMapping("/leaves/{leaveId}/approve")
    @Operation(summary = "Approve Leave Request", description = "Approve a pending leave request with optional comments.")
    public ResponseEntity<LeaveResponse> approveLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @Valid @RequestBody ReviewLeaveRequest request) {

        Long managerId = extractUserIdFromAuthentication(authentication);
        String comments = request.getComments() != null ? request.getComments() : "";
        
        LeaveResponse response = managerService.approveLeave(managerId, leaveId, comments);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/leaves/{leaveId}/reject")
    @Operation(summary = "Reject Leave Request", description = "Reject a pending leave request with optional comments (reason).")
    public ResponseEntity<LeaveResponse> rejectLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @Valid @RequestBody ReviewLeaveRequest request) {

        Long managerId = extractUserIdFromAuthentication(authentication);
        String comments = request.getComments() != null ? request.getComments() : "";
        
        LeaveResponse response = managerService.rejectLeave(managerId, leaveId, comments);
        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        String email = userDetails.getUsername();

        if (userDetails instanceof com.leaveflow.backend.security.user.UserPrincipal) {
            return ((com.leaveflow.backend.security.user.UserPrincipal) userDetails).getId();
        }
        
        throw new IllegalStateException("Unable to extract userId from authentication");
    }
}