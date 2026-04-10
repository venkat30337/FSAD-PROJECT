package com.courseflow.dto.response;

import com.courseflow.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String studentId;
    private String department;
    private String phone;
    private Integer semester;
    private Integer maxCredits;
    private Boolean isActive;
    private Integer usedCredits;
}
