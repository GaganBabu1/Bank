package com.example.banking.dto;

import java.util.List;

public record LowBalancePredictionResponse(
        boolean warning,
        int estimatedDaysUntilThreshold,
        double currentBalance,
        double projectedBalance,
        double threshold,
        String message,
        List<String> reasons
) {
}
