package com.courseflow.dto.response;

import com.courseflow.model.SemesterTerm;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SectionResponse {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Integer courseCredits;
    private String sectionCode;
    private String room;
    private String daysOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxSeats;
    private Integer enrolledCount;
    private String academicYear;
    private SemesterTerm semesterTerm;
}
