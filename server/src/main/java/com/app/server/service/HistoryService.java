package com.app.server.service;

import com.app.server.entity.ExchangeRateHistory;
import com.app.server.repository.ExchangeRateHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final ExchangeRateHistoryRepository historyRepository;

    public HistoryService(ExchangeRateHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public ExchangeRateHistory addEntry(String baseCurrency, String targetCurrency, double rate) {
        if ("INR".equals(baseCurrency) || "INR".equals(targetCurrency)) {
            ExchangeRateHistory history = new ExchangeRateHistory();
            history.setBaseCurrency(baseCurrency.toUpperCase());
            history.setTargetCurrency(targetCurrency.toUpperCase());
            history.setExchangeRate(rate);
            return historyRepository.save(history);
        }
        return null;
    }

    public List<ExchangeRateHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    public ExchangeRateHistory getLatestEntry(String baseCurrency, String targetCurrency) {
        return historyRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByCreatedAtDesc(
                baseCurrency.toUpperCase(), targetCurrency.toUpperCase()).orElse(null);
    }
}
