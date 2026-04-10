package com.courseflow.controller;

import com.courseflow.dto.request.ConflictCheckRequest;
import com.courseflow.dto.request.EnrollmentRequest;
import com.courseflow.dto.response.ConflictCheckResponse;
import com.courseflow.dto.response.EnrollmentResponse;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.model.User;
import com.courseflow.service.EnrollmentService;
import com.courseflow.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enroll(Authentication authentication,
                                                     @Valid @RequestBody EnrollmentRequest request) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(enrollmentService.enroll(student.getId(), request.getSectionId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> drop(Authentication authentication, @PathVariable Long id) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(enrollmentService.dropEnrollment(student.getId(), id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments(Authentication authentication) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(student.getId()));
    }

    @GetMapping("/my/schedule")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SectionResponse>> mySchedule(Authentication authentication) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(enrollmentService.getMySchedule(student.getId()));
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EnrollmentResponse>> sectionEnrollments(@PathVariable Long sectionId,
                                                                       @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getSectionEnrollments(sectionId, pageable));
    }

    @GetMapping("/conflicts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EnrollmentResponse>> conflicts(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getConflictingEnrollments(pageable));
    }

    @PostMapping("/check-conflict")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ConflictCheckResponse> checkConflict(Authentication authentication,
                                                               @Valid @RequestBody ConflictCheckRequest request) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(enrollmentService.checkConflict(student.getId(), request.getSectionId()));
    }
}
