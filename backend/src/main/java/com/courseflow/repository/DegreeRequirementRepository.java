package com.courseflow.repository;

import com.courseflow.model.DegreeRequirement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreeRequirementRepository extends JpaRepository<DegreeRequirement, Long> {

    List<DegreeRequirement> findByProgramIgnoreCase(String program);
}
