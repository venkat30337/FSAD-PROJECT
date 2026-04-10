package com.courseflow.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpsertRequest {

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Format: CS301")
    private String code;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000)
    private String description;

    @NotNull(message = "Credits are required")
    @Min(value = 1)
    @Max(value = 6)
    private Integer credits;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Instructor is required")
    @Size(min = 3, max = 120)
    private String instructor;

    private List<Long> prerequisiteIds;
}
