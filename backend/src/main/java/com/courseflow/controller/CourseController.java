package com.courseflow.controller;

import com.courseflow.dto.request.CourseUpsertRequest;
import com.courseflow.dto.response.CourseResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.model.CourseStatus;
import com.courseflow.model.User;
import com.courseflow.service.CourseService;
import com.courseflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> listCourses(
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String instructor,
        @RequestParam(required = false) Integer credits,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        CourseStatus parsedStatus = parseStatus(status);
        return ResponseEntity.ok(courseService.listCourses(
            department,
            parsedStatus,
            instructor,
            credits,
            search,
            pageable,
            false
        ));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<CourseResponse>> listPublicCourses(
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String instructor,
        @RequestParam(required = false) Integer credits,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(courseService.listCourses(
            department,
            null,
            instructor,
            credits,
            search,
            pageable,
            true
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(Authentication authentication,
                                                       @Valid @RequestBody CourseUpsertRequest request) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(courseService.createCourse(admin.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(Authentication authentication,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody CourseUpsertRequest request) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(courseService.updateCourse(admin.getId(), id, request));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> publishCourse(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(courseService.publishCourse(admin.getId(), id));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> archiveCourse(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(courseService.archiveCourse(admin.getId(), id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        courseService.softDelete(admin.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private CourseStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return CourseStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid status value");
        }
    }
}
