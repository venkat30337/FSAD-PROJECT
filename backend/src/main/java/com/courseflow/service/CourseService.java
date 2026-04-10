package com.courseflow.service;

import com.courseflow.dto.request.CourseUpsertRequest;
import com.courseflow.dto.response.CourseResponse;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.dto.response.SimpleCourseResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.exception.ResourceNotFoundException;
import com.courseflow.model.Course;
import com.courseflow.model.CourseStatus;
import com.courseflow.model.Prerequisite;
import com.courseflow.model.User;
import com.courseflow.repository.CourseRepository;
import com.courseflow.repository.PrerequisiteRepository;
import com.courseflow.repository.SectionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final SectionRepository sectionRepository;
    private final UserService userService;
    private final AuditService auditService;
    private final ResponseMapper responseMapper;

    public Page<CourseResponse> listCourses(String department,
                                            CourseStatus status,
                                            String instructor,
                                            Integer credits,
                                            String search,
                                            Pageable pageable,
                                            boolean publicOnly) {
        Page<Course> page = publicOnly
            ? courseRepository.findPublishedFiltered(normalize(department), normalize(instructor), credits, normalize(search), pageable)
            : courseRepository.findFiltered(normalize(department), status, normalize(instructor), credits, normalize(search), pageable);
        return page.map(this::toCourseResponseWithDetails);
    }

    public CourseResponse getCourseById(Long courseId) {
        Course course = requireCourse(courseId);
        return toCourseResponseWithDetails(course);
    }

    @Transactional
    public CourseResponse createCourse(Long adminId, CourseUpsertRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Course code already exists");
        }

        User admin = userService.requireById(adminId);
        Course course = Course.builder()
            .code(request.getCode().trim().toUpperCase())
            .title(request.getTitle().trim())
            .description(request.getDescription().trim())
            .credits(request.getCredits())
            .department(request.getDepartment().trim())
            .instructor(request.getInstructor().trim())
            .status(CourseStatus.DRAFT)
            .createdBy(admin)
            .build();

        Course saved = courseRepository.save(course);
        replacePrerequisites(saved, request.getPrerequisiteIds());
        auditService.log(admin, "COURSE_CREATE", "COURSE", saved.getId(), "Created course " + saved.getCode());
        return toCourseResponseWithDetails(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long adminId, Long courseId, CourseUpsertRequest request) {
        Course course = requireCourse(courseId);
        String normalizedCode = request.getCode().trim().toUpperCase();
        courseRepository.findByCode(normalizedCode)
            .filter(existing -> !existing.getId().equals(courseId))
            .ifPresent(existing -> {
                throw new ConflictException("Course code already exists");
            });

        course.setCode(normalizedCode);
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setCredits(request.getCredits());
        course.setDepartment(request.getDepartment().trim());
        course.setInstructor(request.getInstructor().trim());

        Course saved = courseRepository.save(course);
        replacePrerequisites(saved, request.getPrerequisiteIds());

        User admin = userService.requireById(adminId);
        auditService.log(admin, "COURSE_UPDATE", "COURSE", saved.getId(), "Updated course " + saved.getCode());
        return toCourseResponseWithDetails(saved);
    }

    @Transactional
    public CourseResponse publishCourse(Long adminId, Long courseId) {
        Course course = requireCourse(courseId);
        course.setStatus(CourseStatus.PUBLISHED);
        Course saved = courseRepository.save(course);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "COURSE_PUBLISH", "COURSE", saved.getId(), "Published course " + saved.getCode());
        return toCourseResponseWithDetails(saved);
    }

    @Transactional
    public CourseResponse archiveCourse(Long adminId, Long courseId) {
        Course course = requireCourse(courseId);
        course.setStatus(CourseStatus.ARCHIVED);
        Course saved = courseRepository.save(course);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "COURSE_ARCHIVE", "COURSE", saved.getId(), "Archived course " + saved.getCode());
        return toCourseResponseWithDetails(saved);
    }

    @Transactional
    public void softDelete(Long adminId, Long courseId) {
        Course course = requireCourse(courseId);
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "COURSE_DELETE", "COURSE", course.getId(), "Soft deleted course " + course.getCode());
    }

    public Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private void replacePrerequisites(Course course, List<Long> prerequisiteIds) {
        prerequisiteRepository.deleteByCourseId(course.getId());
        if (prerequisiteIds == null || prerequisiteIds.isEmpty()) {
            return;
        }

        List<Prerequisite> prerequisites = new ArrayList<>();
        for (Long prerequisiteId : prerequisiteIds) {
            if (course.getId().equals(prerequisiteId)) {
                continue;
            }
            Course requiredCourse = requireCourse(prerequisiteId);
            prerequisites.add(Prerequisite.builder().course(course).requiredCourse(requiredCourse).build());
        }
        prerequisiteRepository.saveAll(prerequisites);
    }

    private CourseResponse toCourseResponseWithDetails(Course course) {
        List<SectionResponse> sections = sectionRepository.findByCourseId(course.getId()).stream()
            .map(responseMapper::toSectionResponse)
            .toList();

        List<SimpleCourseResponse> prerequisites = prerequisiteRepository.findByCourseId(course.getId()).stream()
            .map(Prerequisite::getRequiredCourse)
            .map(responseMapper::toSimpleCourseResponse)
            .toList();

        return CourseResponse.builder()
            .id(course.getId())
            .code(course.getCode())
            .title(course.getTitle())
            .description(course.getDescription())
            .credits(course.getCredits())
            .department(course.getDepartment())
            .instructor(course.getInstructor())
            .status(course.getStatus())
            .sections(sections)
            .prerequisites(prerequisites)
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
