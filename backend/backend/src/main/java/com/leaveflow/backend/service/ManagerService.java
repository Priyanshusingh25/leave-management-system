package com.leaveflow.backend.service;

import com.leaveflow.backend.dto.LeaveResponse;
import com.leaveflow.backend.dto.ManagerDashboardResponse;
import com.leaveflow.backend.entity.Employee;
import com.leaveflow.backend.entity.Leave;
import com.leaveflow.backend.enums.LeaveStatus;
import com.leaveflow.backend.exception.BadRequestException;
import com.leaveflow.backend.exception.ResourceNotFoundException;
import com.leaveflow.backend.repository.EmployeeRepository;
import com.leaveflow.backend.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public Page<LeaveResponse> getPendingLeaves(Long managerId, Pageable pageable) {
        log.info("Fetching pending leaves for manager: {}", managerId);
        Page<Leave> leaves = leaveRepository.findByStatusForManager(LeaveStatus.PENDING, managerId, pageable);
        return leaves.map(this::mapToResponse);
    }

    public Page<LeaveResponse> getTeamLeaveHistory(Long managerId, Pageable pageable) {
        log.info("Fetching team leave history for manager: {}", managerId);
        Page<Leave> leaves = leaveRepository.findAllForManagerTeam(managerId, pageable);
        return leaves.map(this::mapToResponse);
    }

    @Transactional
    public LeaveResponse approveLeave(Long managerId, Long leaveId, String comments) {
        log.info("Approving leave with id: {} by manager: {}", leaveId, managerId);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        validateManagerAuthority(leave, managerId);
        validateLeaveStatus(leave, LeaveStatus.PENDING);

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setManagerComments(comments);
        leave.setReviewedBy(manager);

        Leave updatedLeave = leaveRepository.save(leave);
        log.info("Leave approved successfully with id: {}", updatedLeave.getId());
        return mapToResponse(updatedLeave);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long managerId, Long leaveId, String comments) {
        log.info("Rejecting leave with id: {} by manager: {}", leaveId, managerId);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        validateManagerAuthority(leave, managerId);
        validateLeaveStatus(leave, LeaveStatus.PENDING);

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setManagerComments(comments);
        leave.setReviewedBy(manager);

        Leave updatedLeave = leaveRepository.save(leave);
        log.info("Leave rejected successfully with id: {}", updatedLeave.getId());
        return mapToResponse(updatedLeave);
    }

    public ManagerDashboardResponse getManagerDashboard(Long managerId) {
        log.info("Fetching dashboard data for manager: {}", managerId);

        long totalEmployees = employeeRepository.countByManager_Id(managerId);
        long pendingApprovals = leaveRepository.countByManagerIdAndStatus(managerId, LeaveStatus.PENDING);
        long approvedRequests = leaveRepository.countByManagerIdAndStatus(managerId, LeaveStatus.APPROVED);
        long rejectedRequests = leaveRepository.countByManagerIdAndStatus(managerId, LeaveStatus.REJECTED);

        // Recent activity: latest 5 leaves
        Pageable pageable = PageRequest.of(0, 5);
        Page<Leave> recentLeaves = leaveRepository.findAllForManagerTeam(managerId, pageable);
        List<LeaveResponse> recentActivity = recentLeaves.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return ManagerDashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .pendingApprovals(pendingApprovals)
                .approvedRequests(approvedRequests)
                .rejectedRequests(rejectedRequests)
                .recentActivity(recentActivity)
                .build();
    }

    private void validateManagerAuthority(Leave leave, Long managerId) {
        Long leaveManagerId = leave.getEmployee().getManager() != null ? leave.getEmployee().getManager().getId() : null;
        if (!managerId.equals(leaveManagerId)) {
            throw new BadRequestException("You are not authorized to review this leave request.");
        }
    }

    private void validateLeaveStatus(Leave leave, LeaveStatus expectedStatus) {
        if (!leave.getStatus().equals(expectedStatus)) {
            throw new BadRequestException("Only " + expectedStatus.name() + " leaves can be reviewed.");
        }
    }

    private LeaveResponse mapToResponse(Leave leave) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;

        return LeaveResponse.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getName())
                .leaveType(leave.getLeaveType().name())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .numberOfDays(daysBetween)
                .reason(leave.getReason())
                .status(leave.getStatus().name())
                .managerComments(leave.getManagerComments())
                .reviewedByName(leave.getReviewedBy() != null ? leave.getReviewedBy().getName() : null)
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }
}