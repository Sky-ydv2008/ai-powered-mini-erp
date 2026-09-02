package com.example.intellierp.controller;

import com.example.intellierp.entity.AuditLog;
import com.example.intellierp.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Enterprise Audit Trail & System Events")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/recent")
    @Operation(summary = "Get recent 50 audit logs")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }

    @GetMapping
    @Operation(summary = "Get paginated audit logs")
    public ResponseEntity<Page<AuditLog>> getPagedLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditLogService.getAllLogs(PageRequest.of(page, size)));
    }
}
