package com.leaveflow.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewLeaveRequest {
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;
}
