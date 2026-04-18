# payment-processor

Spring Boot 3 payment processor microservice for the PayStream hackathon.

## Features
- Java 17
- Spring Boot 3
- Spring Kafka consumer/producer
- Spring Data JPA with H2
- Bean Validation
- Actuator health/metrics
- REST reporting APIs

## Local run
1. Start Kafka on `localhost:9092`.
2. Create the topics if they do not already exist:
   - `payments.submitted`
   - `payments.processed`
   - 6 partitions each
3. Start the app:

```powershell
./mvnw spring-boot:run
```

## Useful endpoints
- `GET /actuator/health`
- `GET /api/metrics/summary`
- `GET /api/reports/summary`
- `GET /api/reports/activity?status=PROCESSED&accountId=ACC-1001&page=0&size=20`
- `GET /api/accounts/ACC-1001/history`
- H2 console: `/h2-console`

## Notes
- The service uses its own in-memory H2 database.
- Accounts are seeded from `src/main/resources/accounts.json` on startup when the accounts table is empty.
- Kafka JSON messages are consumed from `payments.submitted` and published to `payments.processed`.

