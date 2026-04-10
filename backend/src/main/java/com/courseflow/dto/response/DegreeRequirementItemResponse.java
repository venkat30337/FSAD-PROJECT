package com.courseflow.dto.response;

import com.courseflow.model.RequirementCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DegreeRequirementItemResponse {
    private Long requirementId;
    private RequirementCategory category;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String status;
}
