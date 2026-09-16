package com.example.banking.controller;

import com.example.banking.dto.MoneyHealthResponse;
import com.example.banking.services.AiInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Insights", description = "Explainable financial insight endpoints")
public class InsightController {

    @Autowired
    private AiInsightService aiInsightService;

    @GetMapping("/my-financial-health")
    @Operation(summary = "Get mock financial health", description = "Return a mock, read-only financial health insight")
    public ResponseEntity<MoneyHealthResponse> getMyFinancialHealth(Authentication authentication) {
        Long userId = authentication.getDetails() instanceof Number number
                ? number.longValue()
                : null;
        return ResponseEntity.ok(aiInsightService.getMoneyHealth(userId));
    }
}
