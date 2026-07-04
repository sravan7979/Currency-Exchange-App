package com.app.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddExchangeRateRequest {

    @NotBlank(message = "Base currency cannot be blank")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must contain exactly 3 alphabetic characters.")
    private String baseCurrency;

    @NotBlank(message = "Target currency cannot be blank")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must contain exactly 3 alphabetic characters.")
    private String targetCurrency;

    @Positive(message = "Rate must be a positive number.")
    private double exchangeRate;
}
