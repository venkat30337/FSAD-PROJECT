package com.courseflow.service;

import com.courseflow.dto.response.EnrollmentResponse;
import com.courseflow.dto.response.SectionResponse;
import com.courseflow.dto.response.SimpleCourseResponse;
import com.courseflow.model.Course;
import com.courseflow.model.Enrollment;
import com.courseflow.model.Section;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public SectionResponse toSectionResponse(Section section) {
        return SectionResponse.builder()
            .id(section.getId())
            .courseId(section.getCourse().getId())
            .courseCode(section.getCourse().getCode())
            .courseTitle(section.getCourse().getTitle())
            .courseCredits(section.getCourse().getCredits())
            .sectionCode(section.getSectionCode())
            .room(section.getRoom())
            .daysOfWeek(section.getDaysOfWeek())
            .startTime(section.getStartTime())
            .endTime(section.getEndTime())
            .maxSeats(section.getMaxSeats())
            .enrolledCount(section.getEnrolledCount())
            .academicYear(section.getAcademicYear())
            .semesterTerm(section.getSemesterTerm())
            .build();
    }

    public SimpleCourseResponse toSimpleCourseResponse(Course course) {
        return SimpleCourseResponse.builder()
            .id(course.getId())
            .code(course.getCode())
            .title(course.getTitle())
            .build();
    }

    public EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
            .id(enrollment.getId())
            .studentId(enrollment.getStudent().getId())
            .studentName(enrollment.getStudent().getFullName())
            .status(enrollment.getStatus())
            .waitlistPos(enrollment.getWaitlistPos())
            .enrolledAt(enrollment.getEnrolledAt())
            .droppedAt(enrollment.getDroppedAt())
            .section(toSectionResponse(enrollment.getSection()))
            .build();
    }
}
