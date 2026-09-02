package com.example.intellierp.controller;

import com.example.intellierp.util.DataSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "System Administration & Demo Management")
public class SettingsController {

    @Autowired
    private DataSeeder dataSeeder;

    @PostMapping("/reseed-data")
    @Operation(summary = "Re-seed realistic demo dataset", description = "Re-populates 50+ products, suppliers, customers, sales, and AI anomaly signals")
    public ResponseEntity<Map<String, String>> reseedData() {
        dataSeeder.seedDatabase();
        return ResponseEntity.ok(Map.of("message", "Database successfully re-seeded with demo records"));
    }
}
