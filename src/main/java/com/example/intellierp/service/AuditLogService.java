package com.example.intellierp.service;

import com.example.intellierp.entity.AuditLog;
import com.example.intellierp.entity.User;
import com.example.intellierp.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(User user, String action, String entityName, Long entityId, String description, String ipAddress) {
        try {
            AuditLog log = new AuditLog(user, action, entityName, entityId, description, ipAddress);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Ensure logging failure does not disrupt primary transaction
        }
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
