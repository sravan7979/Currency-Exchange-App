const BASE_URL = 'http://10.136.14.166:8080';

async function request(endpoint, options = {}) {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    let errorMessage = `HTTP Error: ${response.status}`;

    try {
      const error = await response.json();
      errorMessage = error.message || JSON.stringify(error);
    } catch (_) {}

    throw new Error(errorMessage);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const getCacheEntries = () =>
  request('/cache');

export const getCacheStatistics = () =>
  request('/cache/statistics');

export const getHistory = () =>
  request('/history');

export const getLatestHistory = (targetCurrency) =>
  request('/history/latest', {
    method: 'POST',
    body: JSON.stringify({ targetCurrency }),
  });

export const addExchangeRate = (targetCurrency, exchangeRate) =>
  request('/exchange-rates', {
    method: 'POST',
    body: JSON.stringify({
      targetCurrency,
      exchangeRate,
    }),
  });

export const resolveExchangeRate = (sourceCurrency, targetCurrency) =>
  request('/exchange-rates/get', {
    method: 'POST',
    body: JSON.stringify({
      sourceCurrency,
      targetCurrency,
    }),
  });

export const removeStaleCache = () =>
  request('/cache/stale', {
    method: 'DELETE',
  });

export const clearCache = () =>
  request('/cache', {
    method: 'DELETE',
  });