package com.courseflow.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleCourseResponse {
    private Long id;
    private String code;
    private String title;
}
