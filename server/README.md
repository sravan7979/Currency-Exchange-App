# Server

This is the Spring Boot backend for the Currency Exchange Cache application. 
Spring Boot provides the REST APIs, MySQL stores the persistent exchange rate history, and an in-memory `HashMap` is used as the cache. The backend is designed to communicate with the React frontend.

## Tech Stack
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Jakarta Validation

## Project Structure
- `config`
- `controller`
- `dto`
- `entity`
- `repository`
- `service`
- `cache`
- `exception`
- `util`

## Project Architecture

React Frontend
↓
REST Controllers
↓
Services
↓
MySQL (History)
and
↓
HashMap Cache

History is the source of truth. 
Cache is only an in-memory optimization layer.

## Base Currency
INR is the fixed base currency.
Users only add the Target Currency and Exchange Rate.
The application automatically stores INR → Target Currency.

## Database
There is only one table: `exchange_rate_history`
Its purpose is to store the persistent history of all direct exchange rates.

Fields:
- `id`
- `baseCurrency`
- `targetCurrency`
- `exchangeRate`
- `createdAt`
- `updatedAt`

## Cache
The cache is implemented using a `HashMap<String, CacheEntry>`.
- **TTL**: 2 Minutes
- **Maximum Size**: 50 Entries

Cache stores:
- Direct INR-based exchange rates
- Previously derived exchange rates

Cache entries are temporary and disappear when the server stops.

## Lookup Flow
Request
↓
Check Cache
↓
Cache Hit
↓
Return
↓
Cache Miss
↓
Check Database
↓
Found
↓
Update Cache
↓
Return
↓
Not Found
↓
Cross-rate Derivation using INR
↓
Cache Derived Rate
↓
Return

## Cross-Rate Derivation
Example:

History has:
INR → USD
INR → JPY

User requests:
USD → JPY

Application calculates:
USD → INR
↓
USD → JPY

The derived exchange rate is cached for future requests.

## REST API

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | `/exchange-rates` | Add a new direct exchange rate. |
| POST | `/exchange-rates/get` | Resolve and return an exchange rate (direct or derived). |
| PUT | `/exchange-rates` | Update an existing exchange rate. |
| DELETE | `/exchange-rates` | Invalidate a specific exchange rate from the cache. |
| GET | `/exchange-rates` | Get all direct INR-based rates currently in the cache. |
| GET | `/cache` | Retrieve all current entries stored in the cache. |
| GET | `/cache/statistics` | Retrieve detailed cache statistics and metrics. |
| DELETE | `/cache/stale` | Manually remove all stale entries from the cache. |
| DELETE | `/cache` | Clear all entries from the cache entirely. |
| GET | `/history` | Retrieve the complete historical log of exchange rates. |
| POST | `/history/latest` | Retrieve the latest historical entry for a given target currency. |

## Cache Statistics
- **Cache Hits**: Number of times an entry was successfully found in the cache and was fresh.
- **Cache Misses**: Number of times an entry was not found in the cache or was stale.
- **Current Cache Size**: The current number of entries stored in the cache.
- **Maximum Cache Size**: The hard limit of entries allowed in the cache (50).
- **Derived Rates**: The current number of derived cross-rates present in the cache.
- **Fresh Entries**: The number of cache entries that have not exceeded their TTL.
- **Stale Entries**: The number of cache entries that have exceeded their TTL.
- **History Records**: The total number of historical rate records stored in the database.
- **Refresh Count**: The number of times the cache was refreshed from the history source of truth.

## Features
✔ RESTful API
✔ MySQL Persistence
✔ In-Memory Cache
✔ TTL-based Cache
✔ Cross-Rate Derivation
✔ Cache Statistics
✔ Cache Invalidation
✔ History Tracking
✔ Layered Architecture

## Running the Server

1. Configure `application.properties`.
2. Create the MySQL database `currency_exchange_cache`.
3. Run the following command:

```bash
./mvnw spring-boot:run
```
or 
```bash
mvn spring-boot:run
```

## Future Improvements
- Redis Cache
- Scheduled Refresh
- External Exchange Rate API
- Authentication
- Docker
