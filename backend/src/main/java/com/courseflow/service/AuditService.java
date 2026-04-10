package com.courseflow.service;

import com.courseflow.model.AuditLog;
import com.courseflow.model.User;
import com.courseflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User actor, String action, String entityType, Long entityId, String detail) {
        AuditLog log = AuditLog.builder()
            .admin(actor)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .detail(detail)
            .build();
        auditLogRepository.save(log);
    }
}
