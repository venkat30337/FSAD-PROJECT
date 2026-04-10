package com.courseflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "section_code", nullable = false, length = 20)
    private String sectionCode;

    @Column(length = 60)
    private String room;

    @Column(name = "days_of_week", nullable = false, length = 50)
    private String daysOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    @Column(name = "enrolled_count", nullable = false)
    private Integer enrolledCount;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester_term", nullable = false, length = 8)
    private SemesterTerm semesterTerm;
}
