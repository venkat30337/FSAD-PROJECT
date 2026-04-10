package com.courseflow.controller;

import com.courseflow.dto.request.SectionRequest;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.model.User;
import com.courseflow.service.SectionService;
import com.courseflow.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final UserService userService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<SectionResponse>> getSectionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> createSection(Authentication authentication,
                                                         @Valid @RequestBody SectionRequest request) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(sectionService.createSection(admin.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> updateSection(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody SectionRequest request) {
        User admin = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(sectionService.updateSection(admin.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSection(Authentication authentication, @PathVariable Long id) {
        User admin = userService.requireByEmail(authentication.getName());
        sectionService.deleteSection(admin.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<Map<String, Object>> getSeatAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.seatAvailability(id));
    }
}
