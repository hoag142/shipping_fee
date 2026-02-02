# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvnw.cmd clean package

# Run
mvnw.cmd spring-boot:run

# Run all tests
mvnw.cmd test

# Run a single test class
mvnw.cmd test -Dtest=ShippingFeeApplicationTests
```

The app runs on port 8080 by default.

## Architecture

Spring Boot 4.0.2 REST API (Java 17, Maven) for calculating shipping fees in Vietnam, integrating with the GHN (Giao Hàng Nhanh) shipping carrier API.

**Layers:**
- **Controller** (`controller/ShippingController.java`) — REST endpoints under `/api/shipping` for provinces, districts, wards, and fee calculation
- **Service** (`service/GhtkService.java`) — Business logic and GHN API integration via RestTemplate. Falls back to hardcoded mock data when the API token is missing or the API call fails.
- **DTO** (`dto/`) — Request/response objects including `ApiResponse<T>` generic wrapper, `ShippingRequest`, `ShippingResponse`, and location DTOs (Province, District, Ward)
- **Config** (`config/WebConfig.java`) — CORS setup for frontend dev servers (ports 3000, 5173)

**External API:** GHN gateway configured in `application.yaml` under `ghtk.api.*` (token, shop-id, base-url). Dev endpoint: `dev-online-gateway.ghn.vn`.

**Key conventions:**
- Lombok throughout: `@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder`
- Vietnamese comments and mock data (63 provinces, sample districts/wards)
- No linter or formatter configured
