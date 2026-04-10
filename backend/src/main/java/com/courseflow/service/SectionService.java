package com.courseflow.service;

import com.courseflow.dto.request.SectionRequest;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.exception.ResourceNotFoundException;
import com.courseflow.model.Section;
import com.courseflow.model.User;
import com.courseflow.repository.SectionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final AuditService auditService;
    private final ResponseMapper responseMapper;

    public List<SectionResponse> getSectionsByCourse(Long courseId) {
        courseService.requireCourse(courseId);
        return sectionRepository.findByCourseId(courseId).stream()
            .map(responseMapper::toSectionResponse)
            .toList();
    }

    @Transactional
    public SectionResponse createSection(Long adminId, SectionRequest request) {
        validateTimes(request);
        Section section = Section.builder()
            .course(courseService.requireCourse(request.getCourseId()))
            .sectionCode(request.getSectionCode().trim())
            .room(request.getRoom() == null ? null : request.getRoom().trim())
            .daysOfWeek(request.getDaysOfWeek().trim().toUpperCase())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .maxSeats(request.getMaxSeats())
            .enrolledCount(0)
            .academicYear(request.getAcademicYear().trim())
            .semesterTerm(request.getSemesterTerm())
            .build();

        Section saved = sectionRepository.save(section);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "SECTION_CREATE", "SECTION", saved.getId(), "Created section " + saved.getSectionCode());
        return responseMapper.toSectionResponse(saved);
    }

    @Transactional
    public SectionResponse updateSection(Long adminId, Long sectionId, SectionRequest request) {
        validateTimes(request);
        Section section = requireSection(sectionId);

        section.setCourse(courseService.requireCourse(request.getCourseId()));
        section.setSectionCode(request.getSectionCode().trim());
        section.setRoom(request.getRoom() == null ? null : request.getRoom().trim());
        section.setDaysOfWeek(request.getDaysOfWeek().trim().toUpperCase());
        section.setStartTime(request.getStartTime());
        section.setEndTime(request.getEndTime());
        section.setMaxSeats(request.getMaxSeats());
        section.setAcademicYear(request.getAcademicYear().trim());
        section.setSemesterTerm(request.getSemesterTerm());

        if (section.getEnrolledCount() > section.getMaxSeats()) {
            throw new ConflictException("Cannot reduce max seats below current enrolled count");
        }

        Section saved = sectionRepository.save(section);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "SECTION_UPDATE", "SECTION", saved.getId(), "Updated section " + saved.getSectionCode());
        return responseMapper.toSectionResponse(saved);
    }

    @Transactional
    public void deleteSection(Long adminId, Long sectionId) {
        Section section = requireSection(sectionId);
        sectionRepository.delete(section);
        User admin = userService.requireById(adminId);
        auditService.log(admin, "SECTION_DELETE", "SECTION", sectionId, "Deleted section " + section.getSectionCode());
    }

    public Map<String, Object> seatAvailability(Long sectionId) {
        Section section = requireSection(sectionId);
        Map<String, Object> result = new HashMap<>();
        result.put("sectionId", section.getId());
        result.put("courseCode", section.getCourse().getCode());
        result.put("enrolledCount", section.getEnrolledCount());
        result.put("maxSeats", section.getMaxSeats());
        result.put("remainingSeats", Math.max(0, section.getMaxSeats() - section.getEnrolledCount()));
        return result;
    }

    public Section requireSection(Long sectionId) {
        return sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
    }

    private void validateTimes(SectionRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
            && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new ConflictException("End time must be after start time");
        }
    }
}
