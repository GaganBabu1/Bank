package com.example.banking.dto;

import java.util.List;

public record MoneyHealthResponse(
        int score,
        String status,
        List<String> reasons
) {
}
