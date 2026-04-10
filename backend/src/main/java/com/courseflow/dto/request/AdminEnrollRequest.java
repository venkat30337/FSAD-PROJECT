package com.courseflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEnrollRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Section ID is required")
    private Long sectionId;
}
