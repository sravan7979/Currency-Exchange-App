package com.app.server.controller;

import com.app.server.dto.AddExchangeRateRequest;
import com.app.server.dto.DeleteExchangeRateRequest;
import com.app.server.dto.ExchangeRateResponse;
import com.app.server.dto.GetExchangeRateRequest;
import com.app.server.dto.UpdateExchangeRateRequest;
import com.app.server.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> addExchangeRate(@Valid @RequestBody AddExchangeRateRequest request) {
        ExchangeRateResponse response = exchangeRateService.addOrUpdateRate(request.getTargetCurrency(), request.getExchangeRate());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/get")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(@Valid @RequestBody GetExchangeRateRequest request) {
        ExchangeRateResponse response = exchangeRateService.resolveLookupRate(request.getSourceCurrency(), request.getTargetCurrency());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ExchangeRateResponse> updateExchangeRate(@Valid @RequestBody UpdateExchangeRateRequest request) {
        ExchangeRateResponse response = exchangeRateService.addOrUpdateRate(request.getTargetCurrency(), request.getExchangeRate());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteExchangeRate(@Valid @RequestBody DeleteExchangeRateRequest request) {
        exchangeRateService.invalidateRate(request.getTargetCurrency());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> getAllDirectRates() {
        return ResponseEntity.ok(exchangeRateService.getAllDirectRates());
    }
}
