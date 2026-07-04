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
public class EmployeeDashboardResponse {
    private long totalRequests;
    private long approvedRequests;
    private long pendingRequests;
    private long rejectedRequests;
    private List<LeaveResponse> recentActivity;
}
