package com.courseflow.repository;

import com.courseflow.model.AuditLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        select a from AuditLog a
        where (:action is null or lower(a.action) like lower(concat('%', :action, '%')))
          and (:start is null or a.createdAt >= :start)
          and (:end is null or a.createdAt <= :end)
    """)
    Page<AuditLog> findFiltered(@Param("action") String action,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                Pageable pageable);
}
