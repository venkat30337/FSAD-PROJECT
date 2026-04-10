package com.courseflow.repository;

import com.courseflow.model.Course;
import com.courseflow.model.CourseStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
        select c from Course c
        where (:department is null or lower(c.department) = lower(:department))
          and (:status is null or c.status = :status)
          and (:instructor is null or lower(c.instructor) like lower(concat('%', :instructor, '%')))
          and (:credits is null or c.credits = :credits)
          and (:search is null or lower(c.title) like lower(concat('%', :search, '%')) or lower(c.code) like lower(concat('%', :search, '%')))
    """)
    Page<Course> findFiltered(@Param("department") String department,
                              @Param("status") CourseStatus status,
                              @Param("instructor") String instructor,
                              @Param("credits") Integer credits,
                              @Param("search") String search,
                              Pageable pageable);

    @Query("""
        select c from Course c
        where c.status = com.courseflow.model.CourseStatus.PUBLISHED
          and (:department is null or lower(c.department) = lower(:department))
          and (:instructor is null or lower(c.instructor) like lower(concat('%', :instructor, '%')))
          and (:credits is null or c.credits = :credits)
          and (:search is null or lower(c.title) like lower(concat('%', :search, '%')) or lower(c.code) like lower(concat('%', :search, '%')))
    """)
    Page<Course> findPublishedFiltered(@Param("department") String department,
                                       @Param("instructor") String instructor,
                                       @Param("credits") Integer credits,
                                       @Param("search") String search,
                                       Pageable pageable);

    long countByStatus(CourseStatus status);
}
