package com.courseflow.service;

import com.courseflow.dto.response.AdminDashboardResponse;
import com.courseflow.dto.response.AuditLogResponse;
import com.courseflow.dto.response.EnrollmentResponse;
import com.courseflow.dto.response.UserSummaryResponse;
import com.courseflow.model.AuditLog;
import com.courseflow.model.CourseStatus;
import com.courseflow.model.EnrollmentStatus;
import com.courseflow.model.Section;
import com.courseflow.model.User;
import com.courseflow.model.UserRole;
import com.courseflow.repository.AuditLogRepository;
import com.courseflow.repository.CourseRepository;
import com.courseflow.repository.EnrollmentRepository;
import com.courseflow.repository.SectionRepository;
import com.courseflow.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final AuditService auditService;

    public Page<UserSummaryResponse> getUsers(UserRole role, String department, Boolean active, Pageable pageable) {
        return userRepository.findFiltered(role, normalize(department), active, pageable)
            .map(user -> UserSummaryResponse.builder()
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
                .usedCredits(enrollmentRepository.getCurrentEnrolledCredits(user.getId()))
                .build());
    }

    public Map<String, Object> getUserDetail(Long userId) {
        User user = userService.requireById(userId);
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("user", UserSummaryResponse.builder()
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
            .usedCredits(enrollmentRepository.getCurrentEnrolledCredits(user.getId()))
            .build());
        response.put("enrollments", enrollments);
        return response;
    }

    @Transactional
    public UserSummaryResponse toggleActive(Long adminId, Long userId) {
        User admin = userService.requireById(adminId);
        User user = userService.requireById(userId);
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        User saved = userRepository.save(user);

        auditService.log(admin, "TOGGLE_ACTIVE", "USER", userId,
            "Set user active status to " + saved.getIsActive());

        return UserSummaryResponse.builder()
            .id(saved.getId())
            .fullName(saved.getFullName())
            .email(saved.getEmail())
            .role(saved.getRole())
            .studentId(saved.getStudentId())
            .department(saved.getDepartment())
            .phone(saved.getPhone())
            .semester(saved.getSemester())
            .maxCredits(saved.getMaxCredits())
            .isActive(saved.getIsActive())
            .usedCredits(enrollmentRepository.getCurrentEnrolledCredits(saved.getId()))
            .build();
    }

    public AdminDashboardResponse getDashboard() {
        List<Section> sections = sectionRepository.findAll();
        Map<Long, Integer> enrolledByCourse = new HashMap<>();
        Map<Long, String> courseCodeById = new HashMap<>();
        Map<Long, String> courseTitleById = new HashMap<>();

        for (Section section : sections) {
            Long courseId = section.getCourse().getId();
            enrolledByCourse.merge(courseId, section.getEnrolledCount(), Integer::sum);
            courseCodeById.put(courseId, section.getCourse().getCode());
            courseTitleById.put(courseId, section.getCourse().getTitle());
        }

        List<Map<String, Object>> topCourses = new ArrayList<>();
        enrolledByCourse.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .forEach(entry -> {
                Map<String, Object> row = new HashMap<>();
                row.put("courseId", entry.getKey());
                row.put("courseCode", courseCodeById.get(entry.getKey()));
                row.put("courseTitle", courseTitleById.get(entry.getKey()));
                row.put("enrollmentCount", entry.getValue());
                topCourses.add(row);
            });

        Page<AuditLog> recentLogs = auditLogRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Map<String, Object>> recentActivity = recentLogs.stream()
            .map(log -> {
                Map<String, Object> row = new HashMap<>();
                row.put("id", log.getId());
                row.put("admin", log.getAdmin().getFullName());
                row.put("action", log.getAction());
                row.put("entityType", log.getEntityType());
                row.put("entityId", log.getEntityId());
                row.put("createdAt", log.getCreatedAt());
                row.put("detail", log.getDetail());
                return row;
            })
            .toList();

        return AdminDashboardResponse.builder()
            .totalStudents(userRepository.countByRole(UserRole.STUDENT))
            .totalCourses(courseRepository.count())
            .activeEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.ENROLLED))
            .waitlistedStudents(enrollmentRepository.countByStatus(EnrollmentStatus.WAITLISTED))
            .publishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED))
            .draftCourses(courseRepository.countByStatus(CourseStatus.DRAFT))
            .topCoursesByEnrollment(topCourses)
            .recentActivity(recentActivity)
            .build();
    }

    public Page<EnrollmentResponse> getConflicts(Pageable pageable) {
        return enrollmentService.getConflictingEnrollments(pageable);
    }

    @Transactional
    public EnrollmentResponse manualEnroll(Long adminId, Long studentId, Long sectionId) {
        return enrollmentService.adminEnroll(adminId, studentId, sectionId);
    }

    @Transactional
    public EnrollmentResponse forceDrop(Long adminId, Long enrollmentId) {
        return enrollmentService.adminDrop(adminId, enrollmentId);
    }

    public Page<AuditLogResponse> getAuditLogs(String action,
                                               LocalDateTime start,
                                               LocalDateTime end,
                                               Pageable pageable) {
        return auditLogRepository.findFiltered(normalize(action), start, end, pageable)
            .map(this::toAuditLogResponse);
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
            .id(log.getId())
            .adminId(log.getAdmin().getId())
            .adminName(log.getAdmin().getFullName())
            .action(log.getAction())
            .entityType(log.getEntityType())
            .entityId(log.getEntityId())
            .detail(log.getDetail())
            .createdAt(log.getCreatedAt())
            .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
