package com.app.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private String baseCurrency;
    private String targetCurrency;
    private Double exchangeRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
