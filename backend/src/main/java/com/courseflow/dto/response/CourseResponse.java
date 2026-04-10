package com.courseflow.dto.response;

import com.courseflow.model.CourseStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Integer credits;
    private String department;
    private String instructor;
    private CourseStatus status;
    private List<SectionResponse> sections;
    private List<SimpleCourseResponse> prerequisites;
}
