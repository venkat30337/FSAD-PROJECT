package com.courseflow.repository;

import com.courseflow.model.Prerequisite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrerequisiteRepository extends JpaRepository<Prerequisite, Long> {

    List<Prerequisite> findByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);
}
