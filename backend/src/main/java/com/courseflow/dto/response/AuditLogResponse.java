package com.courseflow.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditLogResponse {
    private Long id;
    private Long adminId;
    private String adminName;
    private String action;
    private String entityType;
    private Long entityId;
    private String detail;
    private LocalDateTime createdAt;
}
