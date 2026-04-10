package com.courseflow.service;

import com.courseflow.dto.response.ConflictCheckResponse;
import com.courseflow.dto.response.EnrollmentResponse;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.exception.CreditLimitExceededException;
import com.courseflow.exception.PrerequisiteNotMetException;
import com.courseflow.exception.ResourceNotFoundException;
import com.courseflow.exception.SeatFullException;
import com.courseflow.model.CourseStatus;
import com.courseflow.model.Enrollment;
import com.courseflow.model.EnrollmentStatus;
import com.courseflow.model.NotificationType;
import com.courseflow.model.Prerequisite;
import com.courseflow.model.Section;
import com.courseflow.model.User;
import com.courseflow.repository.EnrollmentRepository;
import com.courseflow.repository.PrerequisiteRepository;
import com.courseflow.repository.SectionRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ResponseMapper responseMapper;

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long sectionId) {
        User student = userService.requireById(studentId);
        Section section = sectionRepository.findByIdForUpdate(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndSectionId(studentId, sectionId)
            .orElseGet(() -> Enrollment.builder().student(student).section(section).build());

        if (enrollment.getId() != null && enrollment.getStatus() != EnrollmentStatus.DROPPED) {
            throw new ConflictException("Student already enrolled in this section");
        }

        if (section.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            throw new ConflictException("Course is not published");
        }

        validatePrerequisites(studentId, section);

        Enrollment conflict = findConflictEnrollment(studentId, section);
        if (conflict != null) {
            throw new ConflictException("Time conflict with " + conflict.getSection().getCourse().getCode());
        }

        Integer currentCredits = enrollmentRepository.getCurrentEnrolledCredits(studentId);
        int usedCredits = currentCredits == null ? 0 : currentCredits;
        if (usedCredits + section.getCourse().getCredits() > student.getMaxCredits()) {
            throw new CreditLimitExceededException("Credit limit exceeded");
        }

        if (section.getEnrolledCount() < section.getMaxSeats()) {
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollment.setWaitlistPos(null);
            section.setEnrolledCount(section.getEnrolledCount() + 1);
            notificationService.create(
                student,
                "Enrollment confirmed",
                "You are enrolled in " + section.getCourse().getCode() + " (Section " + section.getSectionCode() + ").",
                NotificationType.ENROLLMENT
            );
        } else {
            int maxWaitlistPos = enrollmentRepository.getMaxWaitlistPosition(sectionId);
            enrollment.setStatus(EnrollmentStatus.WAITLISTED);
            enrollment.setWaitlistPos(maxWaitlistPos + 1);
            notificationService.create(
                student,
                "Added to waitlist",
                "Seats are full for " + section.getCourse().getCode() + ". You are waitlisted at position " + enrollment.getWaitlistPos() + ".",
                NotificationType.WAITLIST
            );
        }

        enrollment.setDroppedAt(null);
        Section savedSection = sectionRepository.save(section);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        auditService.log(student, "ENROLL", "ENROLLMENT", savedEnrollment.getId(),
            "Student enrolled request for section " + savedSection.getId() + " with status " + savedEnrollment.getStatus());

        return responseMapper.toEnrollmentResponse(savedEnrollment);
    }

    public ConflictCheckResponse checkConflict(Long studentId, Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        Enrollment conflict = findConflictEnrollment(studentId, section);
        if (conflict == null) {
            return ConflictCheckResponse.builder()
                .hasConflict(false)
                .message("No time conflict detected")
                .build();
        }

        return ConflictCheckResponse.builder()
            .hasConflict(true)
            .message("Time conflict detected")
            .conflictingSection(responseMapper.toSectionResponse(conflict.getSection()))
            .build();
    }

    @Transactional
    public EnrollmentResponse dropEnrollment(Long studentId, Long enrollmentId) {
        return dropEnrollmentInternal(studentId, enrollmentId, false);
    }

    @Transactional
    public EnrollmentResponse adminDrop(Long adminId, Long enrollmentId) {
        return dropEnrollmentInternal(adminId, enrollmentId, true);
    }

    public List<EnrollmentResponse> getMyEnrollments(Long studentId) {
        List<EnrollmentStatus> statuses = List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.WAITLISTED, EnrollmentStatus.COMPLETED);
        return enrollmentRepository.findByStudentIdAndStatusIn(studentId, statuses).stream()
            .map(responseMapper::toEnrollmentResponse)
            .toList();
    }

    public List<SectionResponse> getMySchedule(Long studentId) {
        return enrollmentRepository.findByStudentIdAndStatusIn(studentId, List.of(EnrollmentStatus.ENROLLED)).stream()
            .map(Enrollment::getSection)
            .map(responseMapper::toSectionResponse)
            .toList();
    }

    public Page<EnrollmentResponse> getSectionEnrollments(Long sectionId, Pageable pageable) {
        return enrollmentRepository.findBySectionId(sectionId, pageable)
            .map(responseMapper::toEnrollmentResponse);
    }

    public Page<EnrollmentResponse> getConflictingEnrollments(Pageable pageable) {
        return enrollmentRepository.findConflictingEnrollments(pageable)
            .map(responseMapper::toEnrollmentResponse);
    }

    @Transactional
    public EnrollmentResponse adminEnroll(Long adminId, Long studentId, Long sectionId) {
        User admin = userService.requireById(adminId);
        User student = userService.requireById(studentId);

        Section section = sectionRepository.findByIdForUpdate(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (section.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            throw new ConflictException("Course is not published");
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndSectionId(studentId, sectionId)
            .orElseGet(() -> Enrollment.builder().student(student).section(section).build());

        if (enrollment.getId() != null && enrollment.getStatus() != EnrollmentStatus.DROPPED) {
            throw new ConflictException("Student already enrolled in this section");
        }

        if (section.getEnrolledCount() >= section.getMaxSeats()) {
            throw new SeatFullException("Section seats are full; cannot force ENROLLED without capacity");
        }

        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setWaitlistPos(null);
        enrollment.setDroppedAt(null);
        section.setEnrolledCount(section.getEnrolledCount() + 1);

        sectionRepository.save(section);
        Enrollment saved = enrollmentRepository.save(enrollment);

        notificationService.create(
            student,
            "Enrollment updated by admin",
            "You were enrolled in " + section.getCourse().getCode() + " by an admin.",
            NotificationType.ADMIN
        );

        auditService.log(admin, "ADMIN_ENROLL", "ENROLLMENT", saved.getId(),
            "Admin enrolled student " + studentId + " into section " + sectionId);

        return responseMapper.toEnrollmentResponse(saved);
    }

    private void validatePrerequisites(Long studentId, Section section) {
        List<Prerequisite> prerequisites = prerequisiteRepository.findByCourseId(section.getCourse().getId());
        if (prerequisites.isEmpty()) {
            return;
        }

        Set<Long> completedOrEnrolledCourseIds = enrollmentRepository
            .findByStudentIdAndStatusIn(studentId, List.of(EnrollmentStatus.COMPLETED, EnrollmentStatus.ENROLLED)).stream()
            .map(enrollment -> enrollment.getSection().getCourse().getId())
            .collect(Collectors.toSet());

        List<String> missing = prerequisites.stream()
            .filter(prerequisite -> !completedOrEnrolledCourseIds.contains(prerequisite.getRequiredCourse().getId()))
            .map(prerequisite -> prerequisite.getRequiredCourse().getCode())
            .toList();

        if (!missing.isEmpty()) {
            throw new PrerequisiteNotMetException("Missing prerequisites: " + String.join(", ", missing));
        }
    }

    private Enrollment findConflictEnrollment(Long studentId, Section targetSection) {
        List<Enrollment> potential = enrollmentRepository.findPotentialTimeConflicts(
            studentId, targetSection.getStartTime(), targetSection.getEndTime()
        );

        for (Enrollment enrollment : potential) {
            if (enrollment.getSection().getId().equals(targetSection.getId())) {
                continue;
            }
            if (daysOverlap(enrollment.getSection().getDaysOfWeek(), targetSection.getDaysOfWeek())) {
                return enrollment;
            }
        }
        return null;
    }

    private boolean daysOverlap(String d1, String d2) {
        Set<String> left = new HashSet<>(Arrays.stream(d1.split(","))
            .map(String::trim)
            .map(String::toUpperCase)
            .toList());
        Set<String> right = new HashSet<>(Arrays.stream(d2.split(","))
            .map(String::trim)
            .map(String::toUpperCase)
            .toList());
        left.retainAll(right);
        return !left.isEmpty();
    }

    private EnrollmentResponse dropEnrollmentInternal(Long actorUserId, Long enrollmentId, boolean adminAction) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        User actor = userService.requireById(actorUserId);

        if (!adminAction && !enrollment.getStudent().getId().equals(actorUserId)) {
            throw new ResourceNotFoundException("Enrollment not found");
        }

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new ConflictException("Enrollment is already dropped");
        }

        Section section = sectionRepository.findByIdForUpdate(enrollment.getSection().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        EnrollmentStatus previousStatus = enrollment.getStatus();
        Integer droppedWaitlistPosition = enrollment.getWaitlistPos();

        if (previousStatus == EnrollmentStatus.ENROLLED) {
            section.setEnrolledCount(Math.max(0, section.getEnrolledCount() - 1));
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollment.setWaitlistPos(null);
        Enrollment dropped = enrollmentRepository.save(enrollment);

        if (previousStatus == EnrollmentStatus.ENROLLED) {
            Enrollment nextWaitlisted = enrollmentRepository.findFirstBySectionIdAndStatusOrderByWaitlistPosAsc(
                section.getId(), EnrollmentStatus.WAITLISTED
            ).orElse(null);

            if (nextWaitlisted != null) {
                nextWaitlisted.setStatus(EnrollmentStatus.ENROLLED);
                nextWaitlisted.setWaitlistPos(null);
                nextWaitlisted.setDroppedAt(null);
                enrollmentRepository.save(nextWaitlisted);
                section.setEnrolledCount(section.getEnrolledCount() + 1);

                notificationService.create(
                    nextWaitlisted.getStudent(),
                    "Waitlist promoted",
                    "A seat opened in " + section.getCourse().getCode() + ". You are now ENROLLED.",
                    NotificationType.WAITLIST
                );
            }
        }

        sectionRepository.save(section);

        if (previousStatus == EnrollmentStatus.WAITLISTED && droppedWaitlistPosition != null) {
            renumberWaitlist(section.getId());
        } else if (previousStatus == EnrollmentStatus.ENROLLED) {
            renumberWaitlist(section.getId());
        }

        notificationService.create(
            dropped.getStudent(),
            "Enrollment dropped",
            "Your enrollment for " + section.getCourse().getCode() + " was dropped.",
            NotificationType.DROP
        );

        String action = adminAction ? "ADMIN_DROP" : "STUDENT_DROP";
        auditService.log(actor, action, "ENROLLMENT", dropped.getId(), "Dropped enrollment for section " + section.getId());

        return responseMapper.toEnrollmentResponse(dropped);
    }

    private void renumberWaitlist(Long sectionId) {
        List<Enrollment> waitlisted = enrollmentRepository.findBySectionIdAndStatusOrderByWaitlistPosAsc(
            sectionId,
            EnrollmentStatus.WAITLISTED
        );

        int position = 1;
        for (Enrollment enrollment : waitlisted) {
            enrollment.setWaitlistPos(position++);
        }
        if (!waitlisted.isEmpty()) {
            enrollmentRepository.saveAll(waitlisted);
        }
    }
}
