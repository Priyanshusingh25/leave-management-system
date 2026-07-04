package com.leaveflow.backend.service;

import com.leaveflow.backend.dto.LeaveResponse;
import com.leaveflow.backend.entity.Employee;
import com.leaveflow.backend.dto.LeaveRequestDto;
import com.leaveflow.backend.entity.Leave;
import com.leaveflow.backend.enums.LeaveStatus;
import com.leaveflow.backend.enums.LeaveType;
import com.leaveflow.backend.exception.BadRequestException;
import com.leaveflow.backend.exception.ResourceNotFoundException;
import com.leaveflow.backend.exception.UnauthorizedException;
import com.leaveflow.backend.repository.EmployeeRepository;
import com.leaveflow.backend.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveResponse applyLeave(Long employeeId, LeaveRequestDto request) {
        log.info("Applying leave for employee: {}", employeeId);

        validateLeaveDates(request.getStartDate(), request.getEndDate());

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Leave leave = Leave.builder()
                .employee(employee)
                .leaveType(LeaveType.valueOf(request.getLeaveType().toUpperCase()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        Leave savedLeave = leaveRepository.save(leave);
        log.info("Leave applied successfully with id: {}", savedLeave.getId());
        return mapToResponse(savedLeave);
    }

    @Transactional
    public LeaveResponse updateLeave(Long employeeId, Long leaveId, LeaveRequestDto request) {
        log.info("Updating leave with id: {} for employee: {}", leaveId, employeeId);

        validateLeaveDates(request.getStartDate(), request.getEndDate());

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedException("You can only update your own leave requests.");
        }

        if (!leave.getStatus().equals(LeaveStatus.PENDING)) {
            throw new BadRequestException("Only pending leave requests can be edited.");
        }

        leave.setLeaveType(LeaveType.valueOf(request.getLeaveType().toUpperCase()));
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());

        Leave updatedLeave = leaveRepository.save(leave);
        log.info("Leave updated successfully with id: {}", updatedLeave.getId());
        return mapToResponse(updatedLeave);
    }

    @Transactional
    public void cancelLeave(Long employeeId, Long leaveId) {
        log.info("Cancelling leave with id: {} for employee: {}", leaveId, employeeId);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedException("You can only cancel your own leave requests.");
        }

        if (!leave.getStatus().equals(LeaveStatus.PENDING)) {
            throw new BadRequestException("Only pending leave requests can be cancelled.");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
        log.info("Leave cancelled successfully with id: {}", leaveId);
    }

    public LeaveResponse getLeaveById(Long leaveId) {
        log.info("Fetching leave with id: {}", leaveId);
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        return mapToResponse(leave);
    }

    public Page<LeaveResponse> getEmployeeLeaveHistory(Long employeeId, String status, String leaveType, Pageable pageable) {
        log.info("Fetching leave history for employee: {} with status: {} and type: {}", employeeId, status, leaveType);

        LeaveStatus leaveStatus = status != null ? LeaveStatus.valueOf(status.toUpperCase()) : null;
        LeaveType type = leaveType != null ? LeaveType.valueOf(leaveType.toUpperCase()) : null;

        Page<Leave> leaves = leaveRepository.findByEmployeeIdWithFilters(employeeId, leaveStatus, type, pageable);
        return leaves.map(this::mapToResponse);
    }

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must not be after end date.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date must not be in the past.");
        }
    }

    private LeaveResponse mapToResponse(Leave leave) {
        long daysBetween = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;

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
