package com.app.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse {
    private String baseCurrency;
    private String targetCurrency;
    private double exchangeRate;
    private String source;
}
