package com.courseflow.repository;

import com.courseflow.model.User;
import com.courseflow.model.UserRole;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    long countByRole(UserRole role);

    @Query("""
        select u from User u
        where (:role is null or u.role = :role)
          and (:department is null or lower(u.department) = lower(:department))
          and (:active is null or u.isActive = :active)
    """)
    Page<User> findFiltered(@Param("role") UserRole role,
                            @Param("department") String department,
                            @Param("active") Boolean active,
                            Pageable pageable);
}
