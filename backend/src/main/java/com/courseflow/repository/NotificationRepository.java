package com.courseflow.repository;

import com.courseflow.model.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("update Notification n set n.isRead = true where n.user.id = :userId and n.isRead = false")
    int markAllRead(@Param("userId") Long userId);

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
