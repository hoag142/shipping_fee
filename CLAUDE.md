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
- **Controller** `controller/ShippingController.java`) — REST endpoints under `/api/shipping` for provinces, districts, wards, and fee calculation
- **Service** `service/GhtkService.java`) — Business logic and GHN API integration via RestTemplate. Falls back to hardcoded mock data when the API token is missing or the API call fails.
- **DTO** `dto/`) — Request/response objects including `ApiResponse<T>` generic wrapper, `ShippingRequest`, `ShippingResponse`, and location DTOs (Province, District, Ward)
- **Config** `config/WebConfig.java`) — CORS setup for frontend dev servers (ports 3000, 5173)

**External API:** GHN gateway configured in `application.yaml` under `ghtk.api.*` (token, shop-id, base-url). Dev endpoint: `dev-online-gateway.ghn.vn`.

**Key conventions:**
- Lombok throughout: `@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder`
- Vietnamese comments and mock data (63 provinces, sample districts/wards)
- No linter or formatter configured

## Error Handling

**1. Use `ApiResponse<T>` wrapper for all REST responses:**
- Success responses: `ApiResponse.success(data)`
- Error responses: `ApiResponse.error(errorCode, message)`
- Always return appropriate HTTP status codes

**2. Define error codes as constants:**
- Use a dedicated constants class or enum for error codes
- Error codes should be descriptive (e.g., `INVALID_ADDRESS`, `API_TIMEOUT`, `MISSING_REQUIRED_FIELD`)
- Keep error messages user-friendly and translatable

**3. Service layer exception handling:**
- Catch external API exceptions and log them with `@Slf4j`
- Provide fallback behavior when appropriate (e.g., mock data when GHN API fails)
- Wrap low-level exceptions in domain-specific exceptions
- Example:
```java
  try {
      return callGhnApi(request);
  } catch (RestClientException e) {
      log.error("GHN API call failed: {}", e.getMessage());
      return getFallbackResponse();
  }
```

**4. Controller layer exception handling:**
- Use `@ExceptionHandler` or `@ControllerAdvice` for global exception handling
- Never expose stack traces or internal details to clients
- Log exceptions at appropriate levels (ERROR for unexpected, WARN for expected failures)

**5. Validation errors:**
- Use `@Valid` annotation with Bean Validation constraints
- Return field-level error details in response for form validation
- Use HTTP 400 for client errors, 500 for server errors

## Code Comment Rules

**1. Use Javadoc style (`/** */`) for classes and methods:**
- Apply standard Javadoc format for all class and method documentation
- Include `@param`, `@return`, `@throws` tags where applicable
- Example:
```java
  /**
   * Calculates shipping fee between two locations
   * @param request Shipping request containing pickup and delivery addresses
   * @return Shipping fee calculation result
   */
  public ShippingResponse calculateFee(ShippingRequest request) { ... }
```

**2. Use single-line comments (`//`) for inline code explanations:**
- All inline comments must use `//` format
- Place comments above the code block or at the end of the line
- Keep comments concise and meaningful
- Example:
```java
  // Check if API token is configured
  if (apiToken == null) {
      // Fall back to mock data when token is missing
      return getMockData();
  }
```

**3. No decorative characters in comments:**
- DO NOT use decorative symbols like `====`, `----`, `****`, `####`
- Keep comments clean and professional
- Avoid ASCII art or excessive formatting
- ❌ Bad: `// ========== Main Logic ==========`
- ✅ Good: `// Main logic`

**Summary:** Javadoc for documentation, `//` for code comments, no decorative clutter.