package com.courseflow.dto.response;

import com.courseflow.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
    private String department;
    private Integer semester;
    private Integer maxCredits;
}
