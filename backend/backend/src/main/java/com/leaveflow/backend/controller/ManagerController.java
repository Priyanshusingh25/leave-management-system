package com.leaveflow.backend.controller;

import com.leaveflow.backend.dto.LeaveResponse;
import com.leaveflow.backend.dto.ManagerDashboardResponse;
import com.leaveflow.backend.dto.PageResponse;
import com.leaveflow.backend.dto.ReviewLeaveRequest;
import com.leaveflow.backend.service.ManagerService;
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
        // For now, we'll use email and fetch from repo to get ID
        // In a production app, you'd store userId in JWT claims directly
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Placeholder: we need to fetch employee by email to get ID
        // This should be from a security context utility
        // For this assignment, we can use a custom claim if available
        String email = userDetails.getUsername();
        
        // Since we can't easily get ID from UserDetails, this is a limitation
        // We'll need to rely on claims in the JWT token
        // For now, return a dummy value or enhance UserPrincipal
        
        // Better approach: check if Principal has the ID info
        if (userDetails instanceof com.leaveflow.backend.security.user.UserPrincipal) {
            return ((com.leaveflow.backend.security.user.UserPrincipal) userDetails).getId();
        }
        
        // Fallback: shouldn't reach here in normal flow
        throw new IllegalStateException("Unable to extract userId from authentication");
    }
}