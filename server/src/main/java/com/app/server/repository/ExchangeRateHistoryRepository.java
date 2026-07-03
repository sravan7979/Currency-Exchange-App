package com.app.server.repository;

import com.app.server.entity.ExchangeRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateHistoryRepository extends JpaRepository<ExchangeRateHistory, Long> {
    Optional<ExchangeRateHistory> findTopByBaseCurrencyAndTargetCurrencyOrderByCreatedAtDesc(String baseCurrency, String targetCurrency);
}
