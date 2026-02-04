# Shipping Fee Calculator API

A Spring Boot REST API for calculating shipping fees in Vietnam, integrating with the GHN (Giao Hang Nhanh) shipping carrier API.

## Live Demo

**Live Application:** https://shipping-fee-y8d9.onrender.com/

## Related Repositories

| Repository | Description |
|------------|-------------|
| [shipping_fee_client](https://github.com/hoag142/shipping_fee_client) | React Frontend |
| [shipping_fee_deploy](https://github.com/hoag142/shipping_fee_deploy) | Deployment Configuration |

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.2**
- **Maven**
- **Lombok**
- **GHN API Integration**

## Features

- Get list of 63 provinces/cities in Vietnam
- Get districts by province
- Get wards by district
- Calculate shipping fee based on address and package weight

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shipping/provinces` | Get all provinces/cities |
| GET | `/api/shipping/districts/{provinceId}` | Get districts by province ID |
| GET | `/api/shipping/wards/{districtId}` | Get wards by district ID |
| POST | `/api/shipping/calculate` | Calculate shipping fee |
| GET | `/api/shipping/health` | Health check |

### Calculate Fee Request Example

```json
POST /api/shipping/calculate
{
  "toDistrictId": 1442,
  "toWardCode": "20314",
  "weight": 1000,
  "serviceTypeId": 2,
  "insuranceValue": 500000
}
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Configuration

Configure GHN API credentials in `src/main/resources/application.yaml`:

```yaml
ghtk:
  api:
    token: "your-ghn-api-token"
    shop-id: your-shop-id
    base-url: https://online-gateway.ghn.vn
```

### Build & Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The API will be available at `http://localhost:8080`

## Docker

### Build and Run with Docker

```bash
# Build image
docker build -t shipping-fee-api .

# Run container
docker run -p 8080:8080 shipping-fee-api
```

### Environment Variables

You can override configuration using environment variables:

```bash
docker run -p 8080:8080 \
  -e GHTK_API_TOKEN=your-token \
  -e GHTK_API_SHOP_ID=your-shop-id \
  shipping-fee-api
```

## Project Structure

```
src/main/java/com/example/shipping_fee/
├── ShippingFeeApplication.java    # Main application
├── config/
│   └── WebConfig.java             # CORS configuration
├── constant/
│   ├── ErrorMessages.java         # Error message constants
│   └── GhnApiEndpoints.java       # GHN API endpoint constants
├── controller/
│   └── ShippingController.java    # REST endpoints
├── dto/
│   ├── ApiResponse.java           # Generic API response wrapper
│   ├── DistrictDTO.java           # District data
│   ├── ErrorMessageDTO.java       # Error message data
│   ├── ProvinceDTO.java           # Province data
│   ├── ShippingRequest.java       # Shipping calculation request
│   ├── ShippingResponse.java      # Shipping calculation response
│   └── WardDTO.java               # Ward data
└── service/
    └── GhtkService.java           # GHN API integration service
```

## License

This project is open source and available under the MIT License.
