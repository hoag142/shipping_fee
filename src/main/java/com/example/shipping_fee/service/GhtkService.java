package com.example.shipping_fee.service;

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
     * Get list of Provinces/Cities from GHN
     * API: GET /services/shipment/list_pick_add
     *
     * If token is missing or API fails -> return Mock Data
     */
    public List<ProvinceDTO> getProvinces() {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                log.warn("GHN Token not configured, using Mock Data");
                return getMockProvinces();
            }

            String url = ghtkBaseUrl + "/services/address/getprovince";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    List<ProvinceDTO> provinces = new ArrayList<>();
                    
                    for (Map<String, Object> item : data) {
                        ProvinceDTO dto = new ProvinceDTO();
                        dto.setProvinceId((Integer) item.get("ProvinceID"));
                        dto.setProvinceName((String) item.get("ProvinceName"));
                        dto.setCode((String) item.get("Code"));
                        provinces.add(dto);
                    }
                    return provinces;
                }
            }
        } catch (Exception e) {
            log.error("Error calling GHN API to get province list: {}", e.getMessage());
        }

        return getMockProvinces();
    }

    /**
     * Get list of Districts by Province
     */
    public List<DistrictDTO> getDistricts(Integer provinceId) {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                return getMockDistricts(provinceId);
            }

            String url = ghtkBaseUrl + "/services/address/getdistrict?province_id=" + provinceId;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    List<DistrictDTO> districts = new ArrayList<>();
                    
                    for (Map<String, Object> item : data) {
                        DistrictDTO dto = new DistrictDTO();
                        dto.setId((Integer) item.get("id"));
                        dto.setName((String) item.get("name"));
                        dto.setProvinceId(provinceId);
                        districts.add(dto);
                    }
                    return districts;
                }
            }
        } catch (Exception e) {
            log.error("Error calling GHN API to get district list: {}", e.getMessage());
        }

        return getMockDistricts(provinceId);
    }

    /**
     * Get list of Wards by District
     */
    public List<WardDTO> getWards(Integer districtId) {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                return getMockWards(districtId);
            }

            String url = ghtkBaseUrl + "/services/address/getward?district_id=" + districtId;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                    List<WardDTO> wards = new ArrayList<>();
                    
                    for (Map<String, Object> item : data) {
                        WardDTO dto = new WardDTO();
                        dto.setId((Integer) item.get("id"));
                        dto.setName((String) item.get("name"));
                        dto.setDistrictId(districtId);
                        wards.add(dto);
                    }
                    return wards;
                }
            }
        } catch (Exception e) {
            log.error("Error calling GHN API to get ward list: {}", e.getMessage());
        }

        return getMockWards(districtId);
    }

    /**
     * Calculate shipping fee via GHN API
     * API: POST /shiip/public-api/v2/shipping-order/fee
     */
    public ShippingResponse calculateFee(ShippingRequest request) {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                log.warn("GHN Token not configured, using Mock Calculate");
                return calculateMockFee(request);
            }

            String url = ghtkBaseUrl + "/shiip/public-api/v2/shipping-order/fee";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            if (request.getServiceId() != null) {
                requestBody.put("service_id", request.getServiceId());
            }
            if (request.getServiceTypeId() != null) {
                requestBody.put("service_type_id", request.getServiceTypeId());
            }
            requestBody.put("to_district_id", request.getToDistrictId());
            requestBody.put("to_ward_code", request.getToWardCode());
            requestBody.put("weight", request.getWeight());

            // Optional fields
            if (request.getFromDistrictId() != null) {
                requestBody.put("from_district_id", request.getFromDistrictId());
            }
            if (request.getFromWardCode() != null) {
                requestBody.put("from_ward_code", request.getFromWardCode());
            }
            if (request.getLength() != null) {
                requestBody.put("length", request.getLength());
            }
            if (request.getWidth() != null) {
                requestBody.put("width", request.getWidth());
            }
            if (request.getHeight() != null) {
                requestBody.put("height", request.getHeight());
            }
            if (request.getInsuranceValue() != null) {
                requestBody.put("insurance_value", request.getInsuranceValue());
            }
            if (request.getCodValue() != null) {
                requestBody.put("cod_value", request.getCodValue());
            }
            if (request.getCoupon() != null) {
                requestBody.put("coupon", request.getCoupon());
            }

            log.info("Calling GHN API: POST {} with body: {}", url, requestBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Integer code = parseInteger(body.get("code"));

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");

                    return ShippingResponse.builder()
                            .success(true)
                            .message("Fee calculation successful")
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
                } else {
                    return ShippingResponse.builder()
                            .success(false)
                            .message((String) body.get("message"))
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error calling GHN API to calculate fee: {}", e.getMessage());
        }

        return calculateMockFee(request);
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
                new WardDTO(1000 + districtId, "Phường 1", districtId),
                new WardDTO(2000 + districtId, "Phường 2", districtId),
                new WardDTO(3000 + districtId, "Phường 3", districtId),
                new WardDTO(4000 + districtId, "Xã A", districtId),
                new WardDTO(5000 + districtId, "Xã B", districtId)
        );
    }

    /**
     * Mock shipping fee calculation when Token is not configured
     */
    private ShippingResponse calculateMockFee(ShippingRequest request) {
        // Calculate mock fee based on weight
        int baseFee = 15000;
        int weightFee = (request.getWeight() / 500) * 5000;

        // Calculate distance based on district ID
        boolean sameDistrict = request.getFromDistrictId() != null
                && request.getFromDistrictId().equals(request.getToDistrictId());
        int distanceFee = sameDistrict ? 0 : 20000;

        int serviceFee = baseFee + weightFee + distanceFee;

        // Insurance fee = 0.5% of goods value
        int insuranceFee = 0;
        if (request.getInsuranceValue() != null && request.getInsuranceValue() > 0) {
            insuranceFee = (int) (request.getInsuranceValue() * 0.005);
        }

        // COD fee = 1% of COD value
        int codFee = 0;
        if (request.getCodValue() != null && request.getCodValue() > 0) {
            codFee = (int) (request.getCodValue() * 0.01);
        }

        int total = serviceFee + insuranceFee + codFee;

        return ShippingResponse.builder()
                .success(true)
                .message("Fee calculation successful (Mock Data)")
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
