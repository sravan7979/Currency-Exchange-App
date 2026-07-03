package com.app.server.controller;

import com.app.server.dto.HistoryResponse;
import com.app.server.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<HistoryResponse>> getHistory() {
        List<HistoryResponse> responses = historyService.getAllHistory().stream()
                .map(h -> new HistoryResponse(h.getId(), h.getBaseCurrency(), h.getTargetCurrency(), h.getExchangeRate(), h.getCreatedAt(), h.getUpdatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/latest")
    public ResponseEntity<HistoryResponse> getLatestHistory(@RequestBody Map<String, String> body) {
        String baseCurrency = body.getOrDefault("baseCurrency", "INR");
        String targetCurrency = body.get("targetCurrency");
        if (targetCurrency == null) {
            return ResponseEntity.badRequest().build();
        }
        
        var latest = historyService.getLatestEntry(baseCurrency, targetCurrency);
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new HistoryResponse(latest.getId(), latest.getBaseCurrency(), latest.getTargetCurrency(), latest.getExchangeRate(), latest.getCreatedAt(), latest.getUpdatedAt()));
    }
}
