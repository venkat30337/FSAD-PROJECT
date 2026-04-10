package com.courseflow.controller;

import com.courseflow.dto.response.DegreeAuditResponse;
import com.courseflow.dto.response.DegreeRequirementItemResponse;
import com.courseflow.model.User;
import com.courseflow.service.DegreeService;
import com.courseflow.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/degree")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class DegreeController {

    private final DegreeService degreeService;
    private final UserService userService;

    @GetMapping("/audit")
    public ResponseEntity<DegreeAuditResponse> audit(Authentication authentication) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(degreeService.getAudit(student.getId()));
    }

    @GetMapping("/requirements")
    public ResponseEntity<List<DegreeRequirementItemResponse>> requirements(Authentication authentication) {
        User student = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(degreeService.getRequirements(student.getId()));
    }
}
