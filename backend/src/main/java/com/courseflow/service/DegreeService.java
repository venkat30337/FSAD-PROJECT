package com.courseflow.service;

import com.courseflow.dto.response.DegreeAuditResponse;
import com.courseflow.dto.response.DegreeRequirementItemResponse;
import com.courseflow.model.DegreeRequirement;
import com.courseflow.model.Enrollment;
import com.courseflow.model.EnrollmentStatus;
import com.courseflow.model.RequirementCategory;
import com.courseflow.model.User;
import com.courseflow.repository.DegreeRequirementRepository;
import com.courseflow.repository.EnrollmentRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DegreeService {

    private final DegreeRequirementRepository degreeRequirementRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public DegreeAuditResponse getAudit(Long studentId) {
        User student = userService.requireById(studentId);
        String program = student.getDepartment();

        List<DegreeRequirement> requirements = degreeRequirementRepository.findByProgramIgnoreCase(program);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatusIn(
            studentId,
            List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.COMPLETED)
        );

        Set<Long> completedCourseIds = new HashSet<>();
        Set<Long> enrolledCourseIds = new HashSet<>();

        for (Enrollment enrollment : enrollments) {
            Long courseId = enrollment.getSection().getCourse().getId();
            if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                completedCourseIds.add(courseId);
            } else if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                enrolledCourseIds.add(courseId);
            }
        }

        List<DegreeRequirementItemResponse> core = requirements.stream()
            .filter(requirement -> requirement.getCategory() == RequirementCategory.CORE)
            .map(requirement -> toItem(requirement, completedCourseIds, enrolledCourseIds))
            .toList();

        List<DegreeRequirementItemResponse> elective = requirements.stream()
            .filter(requirement -> requirement.getCategory() == RequirementCategory.ELECTIVE)
            .map(requirement -> toItem(requirement, completedCourseIds, enrolledCourseIds))
            .toList();

        List<DegreeRequirementItemResponse> lab = requirements.stream()
            .filter(requirement -> requirement.getCategory() == RequirementCategory.LAB)
            .map(requirement -> toItem(requirement, completedCourseIds, enrolledCourseIds))
            .toList();

        int totalRequiredCredits = requirements.stream()
            .mapToInt(requirement -> requirement.getCourse().getCredits())
            .sum();

        int completedOrEnrolledCredits = requirements.stream()
            .filter(requirement -> completedCourseIds.contains(requirement.getCourse().getId())
                || enrolledCourseIds.contains(requirement.getCourse().getId()))
            .mapToInt(requirement -> requirement.getCourse().getCredits())
            .sum();

        return DegreeAuditResponse.builder()
            .program(program)
            .totalRequiredCredits(totalRequiredCredits)
            .completedOrEnrolledCredits(completedOrEnrolledCredits)
            .core(core)
            .elective(elective)
            .lab(lab)
            .build();
    }

    public List<DegreeRequirementItemResponse> getRequirements(Long studentId) {
        User student = userService.requireById(studentId);
        List<DegreeRequirement> requirements = degreeRequirementRepository.findByProgramIgnoreCase(student.getDepartment());
        return requirements.stream()
            .map(requirement -> DegreeRequirementItemResponse.builder()
                .requirementId(requirement.getId())
                .category(requirement.getCategory())
                .courseId(requirement.getCourse().getId())
                .courseCode(requirement.getCourse().getCode())
                .courseTitle(requirement.getCourse().getTitle())
                .status("NOT ENROLLED")
                .build())
            .toList();
    }

    private DegreeRequirementItemResponse toItem(DegreeRequirement requirement,
                                                 Set<Long> completedCourseIds,
                                                 Set<Long> enrolledCourseIds) {
        String status = "NOT ENROLLED";
        Long courseId = requirement.getCourse().getId();
        if (completedCourseIds.contains(courseId)) {
            status = "COMPLETED";
        } else if (enrolledCourseIds.contains(courseId)) {
            status = "IN-PROGRESS";
        }

        return DegreeRequirementItemResponse.builder()
            .requirementId(requirement.getId())
            .category(requirement.getCategory())
            .courseId(courseId)
            .courseCode(requirement.getCourse().getCode())
            .courseTitle(requirement.getCourse().getTitle())
            .status(status)
            .build();
    }
}
