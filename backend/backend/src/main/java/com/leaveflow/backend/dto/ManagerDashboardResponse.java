package com.leaveflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerDashboardResponse {
    private long totalEmployees;
    private long pendingApprovals;
    private long approvedRequests;
    private long rejectedRequests;
    private List<LeaveResponse> recentActivity;
}
