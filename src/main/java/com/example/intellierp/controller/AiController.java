package com.example.intellierp.controller;

import com.example.intellierp.entity.AiInsight;
import com.example.intellierp.service.AiInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Business Intelligence", description = "Explainable AI Insights, Anomaly Detection, Predictive Analytics & Natural Language Assistant")
public class AiController {

    @Autowired
    private AiInsightService aiInsightService;

    @GetMapping("/insights")
    @Operation(summary = "Get active AI insights", description = "Returns prioritized AI insights across inventory risk, supplier delays, expense anomalies, and profit warnings")
    public ResponseEntity<List<AiInsight>> getActiveInsights() {
        return ResponseEntity.ok(aiInsightService.getActiveInsights());
    }

    @GetMapping("/insights/{id}")
    @Operation(summary = "Get specific AI insight with explainable evidence and root cause")
    public ResponseEntity<AiInsight> getInsightById(@PathVariable Long id) {
        AiInsight insight = aiInsightService.getInsightById(id);
        if (insight == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(insight);
    }

    @PostMapping("/insights/{id}/read")
    @Operation(summary = "Mark AI insight as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        aiInsightService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/insights/{id}/dismiss")
    @Operation(summary = "Dismiss AI insight")
    public ResponseEntity<Void> dismissInsight(@PathVariable Long id) {
        aiInsightService.dismissInsight(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    @Operation(summary = "Run AI intelligence analysis", description = "Executes the statistical and predictive anomaly engine across all ERP data")
    public ResponseEntity<List<AiInsight>> runAiAnalysis() {
        return ResponseEntity.ok(aiInsightService.generateAllInsights());
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask Your Business Data", description = "Natural Language query assistant answering questions on profits, losses, inventory, suppliers, and trends")
    public ResponseEntity<Map<String, Object>> askBusinessAssistant(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        return ResponseEntity.ok(aiInsightService.askBusinessAssistant(query));
    }
}
