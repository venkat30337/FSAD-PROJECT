package com.courseflow.dto.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalStudents;
    private long totalCourses;
    private long activeEnrollments;
    private long waitlistedStudents;
    private long publishedCourses;
    private long draftCourses;
    private List<Map<String, Object>> topCoursesByEnrollment;
    private List<Map<String, Object>> recentActivity;
}
