package com.courseflow.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DegreeAuditResponse {
    private String program;
    private int totalRequiredCredits;
    private int completedOrEnrolledCredits;
    private List<DegreeRequirementItemResponse> core;
    private List<DegreeRequirementItemResponse> elective;
    private List<DegreeRequirementItemResponse> lab;
}
