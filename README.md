# Restful Booker API Tests

Automated API tests for [Restful Booker](https://restful-booker.herokuapp.com), a public practice API for hotel bookings.
The suite covers authentication and the full booking CRUD flow, with test cases derived from formal test design techniques (EP, BVA, Decision Tables, State Transition) and real API defects found and documented along the way.

## Tech stack

| Tool | Purpose |
|---|---|
| Java 17 | Language |
| Maven | Build and dependency management |
| REST Assured | HTTP client and response assertions |
| TestNG | Test runner, data providers |
| Jackson | JSON serialization of request bodies |
| JSON Schema Validator | Response structure validation |
| Allure | Test reporting |

## Project structure

```
src/test/java
├── config/     Reads base URL and credentials from config.properties
├── specs/      Shared RequestSpecification (base URI, JSON headers, logging on failure)
├── models/     Request POJOs
├── utils/      Helpers reused across tests (auth token, test booking creation)
└── tests/      Test classes, one per endpoint
src/test/resources
├── config.properties
├── testng.xml
└── schemas/    JSON schemas for response validation
```

## Running the tests

Requirements: Java 17+ and Maven.

```bash
mvn clean test
```

Run a single class:

```bash
mvn clean test -Dtest=CreateBookingTest
```

## Allure report

Results are written to `allure-results/` on every run. To view the report (requires the [Allure CLI](https://allurereport.org/docs/install/)):

```bash
allure serve allure-results
```

## Test coverage

43 test cases across 8 classes:

| Class | Endpoint | Tests |
|---|---|---|
| `AuthTest` | `POST /auth` | 3 |
| `GetBookingIdsTest` | `GET /booking` | 4 |
| `GetBookingTest` | `GET /booking/{id}` | 6 |
| `CreateBookingTest` | `POST /booking` | 12 |
| `UpdateBookingTest` | `PUT /booking/{id}` | 5 |
| `PartialUpdateBookingTest` | `PATCH /booking/{id}` | 5 |
| `DeleteBookingTest` | `DELETE /booking/{id}` | 4 |
| `BookingLifecycleTest` | Multiple endpoints | 4 |

## Test design techniques

- **Equivalence Partitioning**: valid and invalid input classes (valid/wrong credentials, missing fields, wrong data types).
- **Boundary Value Analysis**: booking ID (0, -1), `totalprice` (-1, 0), checkout vs. checkin date (-1, 0, +1 days), PATCH with an empty body.
- **Decision Tables**: token valid / invalid / empty / missing on DELETE, PUT and PATCH; filter combinations on `GET /booking`.
- **State Transition**: booking lifecycle (created → updated → deleted) and invalid transitions such as updating a deleted booking.

## Bugs found

Tests assert the API's **current** behaviour, and every known deviation is marked with `BUG:` in the test description, so it shows up in the Allure report. If a bug gets fixed, the matching test will fail and should be updated.

| Endpoint | Scenario | Expected | Actual |
|---|---|---|---|
| `POST /auth` | Wrong or empty credentials | 401 | 200 with `"reason": "Bad credentials"` |
| `POST /booking` | Missing required field | 400 | 500 |
| `POST /booking` | Negative `totalprice` | 400 | 200 |
| `POST /booking` | `totalprice` sent as text | 400 | 200, price saved as `null` |
| `POST /booking` | Checkout before or on checkin day | 400 | 200 |
| `PUT` / `PATCH /booking/{id}` | Non-existent or deleted booking | 404 | 405 |
| `DELETE /booking/{id}` | Successful delete | 200 / 204 | 201 |
