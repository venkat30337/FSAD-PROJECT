package com.courseflow.dto.response;

import com.courseflow.model.EnrollmentStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private EnrollmentStatus status;
    private Integer waitlistPos;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private SectionResponse section;
}
