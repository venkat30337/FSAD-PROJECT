package com.courseflow.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConflictCheckResponse {
    private boolean hasConflict;
    private String message;
    private SectionResponse conflictingSection;
}
