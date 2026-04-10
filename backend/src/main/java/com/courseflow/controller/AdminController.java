package com.courseflow.controller;

import com.courseflow.dto.request.AdminEnrollRequest;
import com.courseflow.dto.response.AdminDashboardResponse;
import com.courseflow.dto.response.AuditLogResponse;
import com.courseflow.dto.response.EnrollmentResponse;
import com.courseflow.dto.response.UserSummaryResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.model.User;
import com.courseflow.model.UserRole;
import com.courseflow.service.AdminService;
import com.courseflow.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryResponse>> users(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) Boolean active,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        UserRole parsedRole = parseRole(role);
        return ResponseEntity.ok(adminService.getUsers(parsedRole, department, active, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> userDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserSummaryResponse> toggleActive(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(adminService.toggleActive(admin.getId(), id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/conflicts")
    public ResponseEntity<Page<EnrollmentResponse>> conflicts(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.getConflicts(pageable));
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponse> manualEnroll(Authentication authentication,
                                                           @Valid @RequestBody AdminEnrollRequest request) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(adminService.manualEnroll(admin.getId(), request.getStudentId(), request.getSectionId()));
    }

    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<EnrollmentResponse> forceDrop(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(adminService.forceDrop(admin.getId(), id));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogResponse>> auditLogs(
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);
        return ResponseEntity.ok(adminService.getAuditLogs(action, start, end, pageable));
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid role value");
        }
    }

    private LocalDateTime parseStartDate(String startDate) {
        if (startDate == null || startDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(startDate).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String endDate) {
        if (endDate == null || endDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(endDate).atTime(LocalTime.MAX);
    }
}
