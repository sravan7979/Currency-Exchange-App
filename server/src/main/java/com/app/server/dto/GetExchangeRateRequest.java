package com.app.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetExchangeRateRequest {

    @NotBlank(message = "Source currency cannot be blank")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must contain exactly 3 alphabetic characters.")
    private String sourceCurrency;

    @NotBlank(message = "Target currency cannot be blank")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must contain exactly 3 alphabetic characters.")
    private String targetCurrency;
}
