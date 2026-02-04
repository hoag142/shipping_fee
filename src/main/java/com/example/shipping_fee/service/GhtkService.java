package com.example.shipping_fee.service;

import com.example.shipping_fee.constant.ErrorMessages;
import com.example.shipping_fee.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

    // Mock calculation constants
    private static final int MOCK_BASE_FEE = 15000;
    private static final int MOCK_WEIGHT_UNIT_GRAMS = 500;
    private static final int MOCK_WEIGHT_FEE_PER_UNIT = 5000;
    private static final int MOCK_DISTANCE_FEE = 20000;
    private static final double MOCK_INSURANCE_RATE = 0.005;
    private static final double MOCK_COD_RATE = 0.01;

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
     *
     * If token is missing or API fails -> return Mock Data
     */
    @SuppressWarnings("unchecked")
    public List<ProvinceDTO> getProvinces() {
        if (!isTokenConfigured()) {
            log.warn("GHN Token not configured, using Mock Data");
            return getMockProvinces();
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
        } catch (Exception e) {
            log.error("Error calling GHN API to get province list: {}", e.getMessage());
        }

        return getMockProvinces();
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
     */
    @SuppressWarnings("unchecked")
    public List<DistrictDTO> getDistricts(Integer provinceId) {
        if (!isTokenConfigured()) {
            return getMockDistricts(provinceId);
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
        } catch (Exception e) {
            log.error("Error calling GHN API to get district list: {}", e.getMessage());
        }

        return getMockDistricts(provinceId);
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
     */
    @SuppressWarnings("unchecked")
    public List<WardDTO> getWards(Integer districtId) {
        if (!isTokenConfigured()) {
            return getMockWards(districtId);
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
        } catch (Exception e) {
            log.error("Error calling GHN API to get ward list: {}", e.getMessage());
        }

        return getMockWards(districtId);
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
            log.warn("GHN Token not configured, using Mock Calculate");
            return calculateMockFee(request);
        }

        try {
            return callGhnFeeApi(request);
        } catch (Exception e) {
            log.error("Error calling GHN API to calculate fee: {}", e.getMessage());
        }

        return calculateMockFee(request);
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

        return calculateMockFee(request);
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
     * Mock list of 63 Vietnam Provinces/Cities
     */
    private List<ProvinceDTO> getMockProvinces() {
        return Arrays.asList(
                new ProvinceDTO(1, "Hà Nội", "HN"),
                new ProvinceDTO(2, "Hồ Chí Minh", "HCM"),
                new ProvinceDTO(3, "Đà Nẵng", "DN"),
                new ProvinceDTO(4, "Hải Phòng", "HP"),
                new ProvinceDTO(5, "Cần Thơ", "CT"),
                new ProvinceDTO(6, "An Giang", "AG"),
                new ProvinceDTO(7, "Bà Rịa - Vũng Tàu", "BRVT"),
                new ProvinceDTO(8, "Bắc Giang", "BG"),
                new ProvinceDTO(9, "Bắc Kạn", "BK"),
                new ProvinceDTO(10, "Bạc Liêu", "BL"),
                new ProvinceDTO(11, "Bắc Ninh", "BN"),
                new ProvinceDTO(12, "Bến Tre", "BT"),
                new ProvinceDTO(13, "Bình Định", "BD"),
                new ProvinceDTO(14, "Bình Dương", "BDG"),
                new ProvinceDTO(15, "Bình Phước", "BP"),
                new ProvinceDTO(16, "Bình Thuận", "BTH"),
                new ProvinceDTO(17, "Cà Mau", "CM"),
                new ProvinceDTO(18, "Cao Bằng", "CB"),
                new ProvinceDTO(19, "Đắk Lắk", "DL"),
                new ProvinceDTO(20, "Đắk Nông", "DNG"),
                new ProvinceDTO(21, "Điện Biên", "DB"),
                new ProvinceDTO(22, "Đồng Nai", "DNI"),
                new ProvinceDTO(23, "Đồng Tháp", "DT"),
                new ProvinceDTO(24, "Gia Lai", "GL"),
                new ProvinceDTO(25, "Hà Giang", "HG"),
                new ProvinceDTO(26, "Hà Nam", "HNA"),
                new ProvinceDTO(27, "Hà Tĩnh", "HT"),
                new ProvinceDTO(28, "Hải Dương", "HD"),
                new ProvinceDTO(29, "Hậu Giang", "HAG"),
                new ProvinceDTO(30, "Hòa Bình", "HB"),
                new ProvinceDTO(31, "Hưng Yên", "HY"),
                new ProvinceDTO(32, "Khánh Hòa", "KH"),
                new ProvinceDTO(33, "Kiên Giang", "KG"),
                new ProvinceDTO(34, "Kon Tum", "KT"),
                new ProvinceDTO(35, "Lai Châu", "LC"),
                new ProvinceDTO(36, "Lâm Đồng", "LD"),
                new ProvinceDTO(37, "Lạng Sơn", "LS"),
                new ProvinceDTO(38, "Lào Cai", "LCA"),
                new ProvinceDTO(39, "Long An", "LA"),
                new ProvinceDTO(40, "Nam Định", "ND"),
                new ProvinceDTO(41, "Nghệ An", "NA"),
                new ProvinceDTO(42, "Ninh Bình", "NB"),
                new ProvinceDTO(43, "Ninh Thuận", "NT"),
                new ProvinceDTO(44, "Phú Thọ", "PT"),
                new ProvinceDTO(45, "Phú Yên", "PY"),
                new ProvinceDTO(46, "Quảng Bình", "QB"),
                new ProvinceDTO(47, "Quảng Nam", "QNA"),
                new ProvinceDTO(48, "Quảng Ngãi", "QNG"),
                new ProvinceDTO(49, "Quảng Ninh", "QN"),
                new ProvinceDTO(50, "Quảng Trị", "QT"),
                new ProvinceDTO(51, "Sóc Trăng", "ST"),
                new ProvinceDTO(52, "Sơn La", "SL"),
                new ProvinceDTO(53, "Tây Ninh", "TN"),
                new ProvinceDTO(54, "Thái Bình", "TB"),
                new ProvinceDTO(55, "Thái Nguyên", "TNG"),
                new ProvinceDTO(56, "Thanh Hóa", "TH"),
                new ProvinceDTO(57, "Thừa Thiên Huế", "TTH"),
                new ProvinceDTO(58, "Tiền Giang", "TG"),
                new ProvinceDTO(59, "Trà Vinh", "TV"),
                new ProvinceDTO(60, "Tuyên Quang", "TQ"),
                new ProvinceDTO(61, "Vĩnh Long", "VL"),
                new ProvinceDTO(62, "Vĩnh Phúc", "VP"),
                new ProvinceDTO(63, "Yên Bái", "YB")
        );
    }

    /**
     * Mock list of Districts (sample)
     */
    private List<DistrictDTO> getMockDistricts(Integer provinceId) {
        // Mock some sample districts for Hanoi and HCMC
        if (provinceId == 1) { // Hanoi
            return Arrays.asList(
                    new DistrictDTO(1, "Quận Ba Đình", 1),
                    new DistrictDTO(2, "Quận Hoàn Kiếm", 1),
                    new DistrictDTO(3, "Quận Tây Hồ", 1),
                    new DistrictDTO(4, "Quận Long Biên", 1),
                    new DistrictDTO(5, "Quận Cầu Giấy", 1),
                    new DistrictDTO(6, "Quận Đống Đa", 1),
                    new DistrictDTO(7, "Quận Hai Bà Trưng", 1),
                    new DistrictDTO(8, "Quận Hoàng Mai", 1),
                    new DistrictDTO(9, "Quận Thanh Xuân", 1),
                    new DistrictDTO(10, "Huyện Sóc Sơn", 1)
            );
        } else if (provinceId == 2) { // HCMC
            return Arrays.asList(
                    new DistrictDTO(11, "Quận 1", 2),
                    new DistrictDTO(12, "Quận 3", 2),
                    new DistrictDTO(13, "Quận 4", 2),
                    new DistrictDTO(14, "Quận 5", 2),
                    new DistrictDTO(15, "Quận 7", 2),
                    new DistrictDTO(16, "Quận 10", 2),
                    new DistrictDTO(17, "Quận Bình Thạnh", 2),
                    new DistrictDTO(18, "Quận Gò Vấp", 2),
                    new DistrictDTO(19, "Quận Tân Bình", 2),
                    new DistrictDTO(20, "Thành phố Thủ Đức", 2)
            );
        }

        // Return generic mock for other provinces
        return Arrays.asList(
                new DistrictDTO(100 + provinceId, "Thành phố/Thị xã " + provinceId, provinceId),
                new DistrictDTO(200 + provinceId, "Huyện A", provinceId),
                new DistrictDTO(300 + provinceId, "Huyện B", provinceId)
        );
    }

    /**
     * Mock list of Wards (sample)
     */
    private List<WardDTO> getMockWards(Integer districtId) {
        return Arrays.asList(
                new WardDTO(String.valueOf(1000 + districtId), "Phường 1", districtId),
                new WardDTO(String.valueOf(2000 + districtId), "Phường 2", districtId),
                new WardDTO(String.valueOf(3000 + districtId), "Phường 3", districtId),
                new WardDTO(String.valueOf(4000 + districtId), "Xã A", districtId),
                new WardDTO(String.valueOf(5000 + districtId), "Xã B", districtId)
        );
    }

    /**
     * Mock shipping fee calculation when Token is not configured
     */
    private ShippingResponse calculateMockFee(ShippingRequest request) {
        int weightFee = (request.getWeight() / MOCK_WEIGHT_UNIT_GRAMS) * MOCK_WEIGHT_FEE_PER_UNIT;
        int distanceFee = isSameDistrict(request) ? 0 : MOCK_DISTANCE_FEE;
        int serviceFee = MOCK_BASE_FEE + weightFee + distanceFee;

        int insuranceFee = calculateMockInsuranceFee(request.getInsuranceValue());
        int codFee = calculateMockCodFee(request.getCodValue());
        int total = serviceFee + insuranceFee + codFee;

        return ShippingResponse.builder()
                .success(true)
                .message(ErrorMessages.MSG_FEE_CALCULATION_SUCCESS_MOCK)
                .total(total)
                .serviceFee(serviceFee)
                .insuranceFee(insuranceFee)
                .pickStationFee(0)
                .couponValue(0)
                .r2sFee(0)
                .documentReturn(0)
                .doubleCheck(0)
                .codFee(codFee)
                .pickRemoteAreasFee(0)
                .deliverRemoteAreasFee(0)
                .codFailedFee(0)
                .build();
    }

    /**
     * Check if origin and destination are in the same district
     */
    private boolean isSameDistrict(ShippingRequest request) {
        return request.getFromDistrictId() != null
                && request.getFromDistrictId().equals(request.getToDistrictId());
    }

    /**
     * Calculate mock insurance fee (0.5% of goods value)
     */
    private int calculateMockInsuranceFee(Integer insuranceValue) {
        if (insuranceValue != null && insuranceValue > 0) {
            return (int) (insuranceValue * MOCK_INSURANCE_RATE);
        }
        return 0;
    }

    /**
     * Calculate mock COD fee (1% of COD value)
     */
    private int calculateMockCodFee(Integer codValue) {
        if (codValue != null && codValue > 0) {
            return (int) (codValue * MOCK_COD_RATE);
        }
        return 0;
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
