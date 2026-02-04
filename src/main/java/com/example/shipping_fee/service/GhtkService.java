package com.example.shipping_fee.service;

import com.example.shipping_fee.constant.ErrorMessages;
import com.example.shipping_fee.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for GHN API business logic
 * - Get address lists (Province, District, Ward)
 * - Calculate shipping fee
 */
@Service
@Slf4j
public class GhtkService {

    // API response codes
    private static final int API_SUCCESS_CODE = 200;

    // Validation limits
    private static final int MAX_WEIGHT_GRAMS = 50000;
    private static final int MAX_INSURANCE_VALUE = 5000000;
    private static final int MAX_COD_VALUE = 5000000;
    private static final int MAX_DIMENSION_CM = 200;
    private static final int MIN_SERVICE_TYPE = 1;
    private static final int MAX_SERVICE_TYPE = 3;

    // Error message for missing token
    private static final String ERR_TOKEN_NOT_CONFIGURED = "GHN API token is not configured";

    @Value("${ghtk.api.token:}")
    private String ghtkToken;

    @Value("${ghtk.api.shop-id:0}")
    private Integer shopId;

    @Value("${ghtk.api.base-url:https://dev-online-gateway.ghn.vn}")
    private String ghtkBaseUrl;

    private final RestTemplate restTemplate;

    public GhtkService() {
        this.restTemplate = new RestTemplate();
    }

    public GhtkService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create HttpHeaders with Token and ShopId for GHN API
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghtkToken);
        headers.set("ShopId", String.valueOf(shopId));
        return headers;
    }

    /**
     * Check if API token is configured
     */
    private boolean isTokenConfigured() {
        return ghtkToken != null && !ghtkToken.isEmpty();
    }

    /**
     * Get list of Provinces/Cities from GHN
     * API: GET /shiip/public-api/master-data/province
     * @return List of provinces or empty list with error logged
     * @throws IllegalStateException if token is not configured
     */
    @SuppressWarnings("unchecked")
    public List<ProvinceDTO> getProvinces() {
        if (!isTokenConfigured()) {
            log.error(ERR_TOKEN_NOT_CONFIGURED);
            throw new IllegalStateException(ERR_TOKEN_NOT_CONFIGURED);
        }

        try {
            String url = ghtkBaseUrl + "/shiip/public-api/master-data/province";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Integer code = parseInteger(body.get("code"));

                if (code != null && code == API_SUCCESS_CODE) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    return parseProvinces(data);
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling GHN API to get province list: {}", e.getMessage());
            throw new RuntimeException(ErrorMessages.ERR_API_CALL_FAILED, e);
        }

        return Collections.emptyList();
    }

    /**
     * Parse province data from API response
     */
    private List<ProvinceDTO> parseProvinces(List<Map<String, Object>> data) {
        List<ProvinceDTO> provinces = new ArrayList<>();
        for (Map<String, Object> item : data) {
            ProvinceDTO dto = new ProvinceDTO();
            dto.setProvinceId(parseInteger(item.get("ProvinceID")));
            dto.setProvinceName((String) item.get("ProvinceName"));
            dto.setCode((String) item.get("Code"));
            provinces.add(dto);
        }
        return provinces;
    }

    /**
     * Get list of Districts by Province
     * @param provinceId Province ID to get districts for
     * @return List of districts in the province
     * @throws IllegalStateException if token is not configured
     */
    @SuppressWarnings("unchecked")
    public List<DistrictDTO> getDistricts(Integer provinceId) {
        if (!isTokenConfigured()) {
            log.error(ERR_TOKEN_NOT_CONFIGURED);
            throw new IllegalStateException(ERR_TOKEN_NOT_CONFIGURED);
        }

        try {
            String url = ghtkBaseUrl + "/shiip/public-api/master-data/district?province_id=" + provinceId;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Integer code = parseInteger(body.get("code"));

                if (code != null && code == API_SUCCESS_CODE) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    return parseDistricts(data, provinceId);
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling GHN API to get district list: {}", e.getMessage());
            throw new RuntimeException(ErrorMessages.ERR_API_CALL_FAILED, e);
        }

        return Collections.emptyList();
    }

    /**
     * Parse district data from API response
     */
    private List<DistrictDTO> parseDistricts(List<Map<String, Object>> data, Integer provinceId) {
        List<DistrictDTO> districts = new ArrayList<>();
        for (Map<String, Object> item : data) {
            DistrictDTO dto = new DistrictDTO();
            dto.setId(parseInteger(item.get("DistrictID")));
            dto.setName((String) item.get("DistrictName"));
            dto.setProvinceId(provinceId);
            districts.add(dto);
        }
        return districts;
    }

    /**
     * Get list of Wards by District
     * @param districtId District ID to get wards for
     * @return List of wards in the district
     * @throws IllegalStateException if token is not configured
     */
    @SuppressWarnings("unchecked")
    public List<WardDTO> getWards(Integer districtId) {
        if (!isTokenConfigured()) {
            log.error(ERR_TOKEN_NOT_CONFIGURED);
            throw new IllegalStateException(ERR_TOKEN_NOT_CONFIGURED);
        }

        try {
            String url = ghtkBaseUrl + "/shiip/public-api/master-data/ward?district_id=" + districtId;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Integer code = parseInteger(body.get("code"));

                if (code != null && code == API_SUCCESS_CODE) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    return parseWards(data, districtId);
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling GHN API to get ward list: {}", e.getMessage());
            throw new RuntimeException(ErrorMessages.ERR_API_CALL_FAILED, e);
        }

        return Collections.emptyList();
    }

    /**
     * Parse ward data from API response
     */
    private List<WardDTO> parseWards(List<Map<String, Object>> data, Integer districtId) {
        List<WardDTO> wards = new ArrayList<>();
        for (Map<String, Object> item : data) {
            WardDTO dto = new WardDTO();
            // WardCode can be returned as Integer or String, convert to String
            Object wardCodeObj = item.get("WardCode");
            dto.setWardCode(wardCodeObj != null ? String.valueOf(wardCodeObj) : null);
            dto.setName((String) item.get("WardName"));
            dto.setDistrictId(districtId);
            wards.add(dto);
        }
        return wards;
    }

    /**
     * Validate shipping request and collect all field errors
     * Note: fromDistrictId and fromWardCode are optional - GHN API uses shop's default address if not provided
     * @param request Shipping request to validate
     * @return List of validation errors (empty if valid)
     */
    public List<ErrorMessageDTO> validateRequest(ShippingRequest request) {
        List<ErrorMessageDTO> errors = new ArrayList<>();

        validateRequiredFields(request, errors);
        validateWeight(request, errors);
        validateServiceType(request, errors);
        validateMonetaryValues(request, errors);
        validateDimensions(request, errors);

        return errors;
    }

    /**
     * Validate required destination fields
     */
    private void validateRequiredFields(ShippingRequest request, List<ErrorMessageDTO> errors) {
        if (request.getToDistrictId() == null) {
            errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_TO_DISTRICT_ID, ErrorMessages.ERR_TO_DISTRICT_REQUIRED));
        }

        if (request.getToWardCode() == null || request.getToWardCode().trim().isEmpty()) {
            errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_TO_WARD_CODE, ErrorMessages.ERR_TO_WARD_REQUIRED));
        }
    }

    /**
     * Validate weight field
     */
    private void validateWeight(ShippingRequest request, List<ErrorMessageDTO> errors) {
        if (request.getWeight() == null) {
            errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_WEIGHT, ErrorMessages.ERR_WEIGHT_REQUIRED));
        } else if (request.getWeight() <= 0) {
            errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_WEIGHT, ErrorMessages.ERR_WEIGHT_INVALID));
        } else if (request.getWeight() > MAX_WEIGHT_GRAMS) {
            errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_WEIGHT, ErrorMessages.ERR_WEIGHT_MAX_EXCEEDED));
        }
    }

    /**
     * Validate service type field
     */
    private void validateServiceType(ShippingRequest request, List<ErrorMessageDTO> errors) {
        if (request.getServiceTypeId() != null) {
            int serviceType = request.getServiceTypeId();
            if (serviceType < MIN_SERVICE_TYPE || serviceType > MAX_SERVICE_TYPE) {
                errors.add(new ErrorMessageDTO(ErrorMessages.FIELD_SERVICE_TYPE_ID, ErrorMessages.ERR_SERVICE_TYPE_INVALID));
            }
        }
    }

    /**
     * Validate monetary values (insurance and COD)
     */
    private void validateMonetaryValues(ShippingRequest request, List<ErrorMessageDTO> errors) {
        validateMonetaryValue(request.getInsuranceValue(), ErrorMessages.FIELD_INSURANCE_VALUE,
                ErrorMessages.ERR_INSURANCE_VALUE_NEGATIVE, ErrorMessages.ERR_INSURANCE_VALUE_MAX_EXCEEDED,
                MAX_INSURANCE_VALUE, errors);

        validateMonetaryValue(request.getCodValue(), ErrorMessages.FIELD_COD_VALUE,
                ErrorMessages.ERR_COD_VALUE_NEGATIVE, ErrorMessages.ERR_COD_VALUE_MAX_EXCEEDED,
                MAX_COD_VALUE, errors);
    }

    /**
     * Validate a single monetary value
     */
    private void validateMonetaryValue(Integer value, String fieldName, String negativeError,
                                       String maxExceededError, int maxValue, List<ErrorMessageDTO> errors) {
        if (value != null) {
            if (value < 0) {
                errors.add(new ErrorMessageDTO(fieldName, negativeError));
            } else if (value > maxValue) {
                errors.add(new ErrorMessageDTO(fieldName, maxExceededError));
            }
        }
    }

    /**
     * Validate package dimensions (length, width, height)
     */
    private void validateDimensions(ShippingRequest request, List<ErrorMessageDTO> errors) {
        validateDimension(request.getLength(), ErrorMessages.FIELD_LENGTH, errors);
        validateDimension(request.getWidth(), ErrorMessages.FIELD_WIDTH, errors);
        validateDimension(request.getHeight(), ErrorMessages.FIELD_HEIGHT, errors);
    }

    /**
     * Validate a single dimension value
     */
    private void validateDimension(Integer value, String fieldName, List<ErrorMessageDTO> errors) {
        if (value != null) {
            if (value < 0) {
                errors.add(new ErrorMessageDTO(fieldName, ErrorMessages.ERR_DIMENSION_NEGATIVE));
            } else if (value > MAX_DIMENSION_CM) {
                errors.add(new ErrorMessageDTO(fieldName, ErrorMessages.ERR_DIMENSION_MAX_EXCEEDED));
            }
        }
    }

    /**
     * Calculate shipping fee via GHN API
     * API: POST /shiip/public-api/v2/shipping-order/fee
     * @param request Shipping request with destination and package info
     * @return Shipping fee calculation result
     */
    @SuppressWarnings("unchecked")
    public ShippingResponse calculateFee(ShippingRequest request) {
        List<ErrorMessageDTO> errors = validateRequest(request);
        if (!errors.isEmpty()) {
            log.warn("Validation failed with {} errors", errors.size());
            return buildValidationErrorResponse(errors);
        }

        if (!isTokenConfigured()) {
            log.error(ERR_TOKEN_NOT_CONFIGURED);
            return ShippingResponse.builder()
                    .success(false)
                    .message(ERR_TOKEN_NOT_CONFIGURED)
                    .build();
        }

        try {
            return callGhnFeeApi(request);
        } catch (Exception e) {
            log.error("Error calling GHN API to calculate fee: {}", e.getMessage());
            return ShippingResponse.builder()
                    .success(false)
                    .message(ErrorMessages.ERR_API_CALL_FAILED + ": " + e.getMessage())
                    .build();
        }
    }

    /**
     * Build validation error response
     */
    private ShippingResponse buildValidationErrorResponse(List<ErrorMessageDTO> errors) {
        return ShippingResponse.builder()
                .success(false)
                .message(ErrorMessages.ERR_VALIDATION_FAILED)
                .errors(errors)
                .build();
    }

    /**
     * Call GHN API to calculate shipping fee
     */
    @SuppressWarnings("unchecked")
    private ShippingResponse callGhnFeeApi(ShippingRequest request) {
        String url = ghtkBaseUrl + "/shiip/public-api/v2/shipping-order/fee";
        Map<String, Object> requestBody = buildFeeRequestBody(request);

        log.info("Calling GHN API: POST {} with body: {}", url, requestBody);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            Integer code = parseInteger(body.get("code"));

            if (code != null && code == API_SUCCESS_CODE) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                return buildSuccessResponse(data);
            } else {
                return ShippingResponse.builder()
                        .success(false)
                        .message((String) body.get("message"))
                        .build();
            }
        }

        return ShippingResponse.builder()
                .success(false)
                .message(ErrorMessages.ERR_API_CALL_FAILED)
                .build();
    }

    /**
     * Build request body for GHN fee API
     */
    private Map<String, Object> buildFeeRequestBody(ShippingRequest request) {
        Map<String, Object> requestBody = new HashMap<>();

        // Required fields
        requestBody.put("to_district_id", request.getToDistrictId());
        requestBody.put("to_ward_code", request.getToWardCode());
        requestBody.put("weight", request.getWeight());

        // Optional service fields
        addIfNotNull(requestBody, "service_id", request.getServiceId());
        addIfNotNull(requestBody, "service_type_id", request.getServiceTypeId());

        // Optional origin fields
        addIfNotNull(requestBody, "from_district_id", request.getFromDistrictId());
        addIfNotNull(requestBody, "from_ward_code", request.getFromWardCode());

        // Optional dimension fields
        addIfNotNull(requestBody, "length", request.getLength());
        addIfNotNull(requestBody, "width", request.getWidth());
        addIfNotNull(requestBody, "height", request.getHeight());

        // Optional value fields
        addIfNotNull(requestBody, "insurance_value", request.getInsuranceValue());
        addIfNotNull(requestBody, "cod_value", request.getCodValue());
        addIfNotNull(requestBody, "coupon", request.getCoupon());

        return requestBody;
    }

    /**
     * Add value to map if not null
     */
    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Build success response from API data
     */
    private ShippingResponse buildSuccessResponse(Map<String, Object> data) {
        return ShippingResponse.builder()
                .success(true)
                .message(ErrorMessages.MSG_FEE_CALCULATION_SUCCESS)
                .total(parseInteger(data.get("total")))
                .serviceFee(parseInteger(data.get("service_fee")))
                .insuranceFee(parseInteger(data.get("insurance_fee")))
                .pickStationFee(parseInteger(data.get("pick_station_fee")))
                .couponValue(parseInteger(data.get("coupon_value")))
                .r2sFee(parseInteger(data.get("r2s_fee")))
                .documentReturn(parseInteger(data.get("document_return")))
                .doubleCheck(parseInteger(data.get("double_check")))
                .codFee(parseInteger(data.get("cod_fee")))
                .pickRemoteAreasFee(parseInteger(data.get("pick_remote_areas_fee")))
                .deliverRemoteAreasFee(parseInteger(data.get("deliver_remote_areas_fee")))
                .codFailedFee(parseInteger(data.get("cod_failed_fee")))
                .build();
    }

    /**
     * Helper: Parse Integer from Object
     */
    private Integer parseInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
