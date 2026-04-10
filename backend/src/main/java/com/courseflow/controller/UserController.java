package com.courseflow.controller;

import com.courseflow.dto.response.UserSummaryResponse;
import com.courseflow.model.User;
import com.courseflow.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> me(Authentication authentication) {
        User user = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(UserSummaryResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole())
            .studentId(user.getStudentId())
            .department(user.getDepartment())
            .phone(user.getPhone())
            .semester(user.getSemester())
            .maxCredits(user.getMaxCredits())
            .isActive(user.getIsActive())
            .usedCredits(0)
            .build());
    }
}
