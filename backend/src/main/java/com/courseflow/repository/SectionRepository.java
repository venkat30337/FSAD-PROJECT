package com.courseflow.repository;

import com.courseflow.model.Section;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCourseId(Long courseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Section s where s.id = :id")
    Optional<Section> findByIdForUpdate(@Param("id") Long id);
}
