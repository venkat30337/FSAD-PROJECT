package com.courseflow.repository;

import com.courseflow.model.Enrollment;
import com.courseflow.model.EnrollmentStatus;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    List<Enrollment> findByStudentIdAndStatusIn(Long studentId, List<EnrollmentStatus> statuses);

    List<Enrollment> findBySectionId(Long sectionId);

    Page<Enrollment> findBySectionId(Long sectionId, Pageable pageable);

    @Query("""
        select e from Enrollment e
        where e.student.id = :studentId
          and e.status = com.courseflow.model.EnrollmentStatus.ENROLLED
          and e.section.startTime < :endTime
          and e.section.endTime > :startTime
    """)
    List<Enrollment> findPotentialTimeConflicts(@Param("studentId") Long studentId,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime);

    @Query("""
        select coalesce(sum(e.section.course.credits), 0)
        from Enrollment e
        where e.student.id = :studentId
          and e.status = com.courseflow.model.EnrollmentStatus.ENROLLED
    """)
    Integer getCurrentEnrolledCredits(@Param("studentId") Long studentId);

    @Query("""
        select coalesce(max(e.waitlistPos), 0)
        from Enrollment e
        where e.section.id = :sectionId
          and e.status = com.courseflow.model.EnrollmentStatus.WAITLISTED
    """)
    Integer getMaxWaitlistPosition(@Param("sectionId") Long sectionId);

    Optional<Enrollment> findFirstBySectionIdAndStatusOrderByWaitlistPosAsc(Long sectionId, EnrollmentStatus status);

    List<Enrollment> findBySectionIdAndStatusOrderByWaitlistPosAsc(Long sectionId, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);

    @Query("""
        select e from Enrollment e
        where e.status = com.courseflow.model.EnrollmentStatus.ENROLLED
          and exists (
            select 1 from Enrollment e2
            where e2.student.id = e.student.id
              and e2.id <> e.id
              and e2.status = com.courseflow.model.EnrollmentStatus.ENROLLED
              and e2.section.daysOfWeek = e.section.daysOfWeek
              and e2.section.startTime < e.section.endTime
              and e2.section.endTime > e.section.startTime
          )
    """)
    Page<Enrollment> findConflictingEnrollments(Pageable pageable);
}
