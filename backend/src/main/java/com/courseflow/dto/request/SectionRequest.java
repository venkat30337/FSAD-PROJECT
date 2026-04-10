package com.courseflow.dto.request;

import com.courseflow.model.SemesterTerm;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Section code is required")
    private String sectionCode;

    private String room;

    @NotBlank(message = "Days of week is required")
    private String daysOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "Max seats should be greater than 0")
    private Integer maxSeats;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Semester term is required")
    private SemesterTerm semesterTerm;
}
