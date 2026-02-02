package com.example.shipping_fee.service;

import com.example.shipping_fee.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * Service xử lý logic nghiệp vụ với GHTK API
 * - Lấy danh sách địa chỉ (Tỉnh, Huyện, Xã)
 * - Tính phí vận chuyển
 */
@Service
@Slf4j
public class GhtkService {

    // GHTK API Token - cấu hình trong application.yaml
    @Value("${ghtk.api.token:}")
    private String ghtkToken;

    // GHTK API Base URL
    @Value("${ghtk.api.base-url:https://services.giaohangtietkiem.vn}")
    private String ghtkBaseUrl;

    private final RestTemplate restTemplate;

    public GhtkService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Tạo HttpHeaders với Token GHTK
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghtkToken);
        return headers;
    }

    // ==================== LẤY DANH SÁCH ĐỊA CHỈ ====================

    /**
     * Lấy danh sách Tỉnh/Thành phố từ GHTK
     * API: GET /services/shipment/list_pick_add
     * 
     * Nếu không có token hoặc API lỗi -> trả về Mock Data
     */
    public List<ProvinceDTO> getProvinces() {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                log.warn("GHTK Token chưa được cấu hình, sử dụng Mock Data");
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
                        dto.setId((Integer) item.get("id"));
                        dto.setName((String) item.get("name"));
                        dto.setCode((String) item.get("code"));
                        provinces.add(dto);
                    }
                    return provinces;
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi GHTK API lấy danh sách tỉnh: {}", e.getMessage());
        }
        
        return getMockProvinces();
    }

    /**
     * Lấy danh sách Quận/Huyện theo Tỉnh
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
            log.error("Lỗi khi gọi GHTK API lấy danh sách huyện: {}", e.getMessage());
        }
        
        return getMockDistricts(provinceId);
    }

    /**
     * Lấy danh sách Phường/Xã theo Quận/Huyện
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
            log.error("Lỗi khi gọi GHTK API lấy danh sách xã: {}", e.getMessage());
        }
        
        return getMockWards(districtId);
    }

    // ==================== TÍNH PHÍ VẬN CHUYỂN ====================

    /**
     * Tính phí vận chuyển qua GHTK API
     * API: GET /services/shipment/fee
     */
    public ShippingResponse calculateFee(ShippingRequest request) {
        try {
            if (ghtkToken == null || ghtkToken.isEmpty()) {
                log.warn("GHTK Token chưa được cấu hình, sử dụng Mock Calculate");
                return calculateMockFee(request);
            }

            String url = UriComponentsBuilder.fromHttpUrl(ghtkBaseUrl + "/services/shipment/fee")
                    .queryParam("pick_province", request.getPickProvince())
                    .queryParam("pick_district", request.getPickDistrict())
                    .queryParamIfPresent("pick_ward", Optional.ofNullable(request.getPickWard()))
                    .queryParamIfPresent("pick_address", Optional.ofNullable(request.getPickAddress()))
                    .queryParam("province", request.getProvince())
                    .queryParam("district", request.getDistrict())
                    .queryParamIfPresent("ward", Optional.ofNullable(request.getWard()))
                    .queryParamIfPresent("address", Optional.ofNullable(request.getAddress()))
                    .queryParam("weight", request.getWeight())
                    .queryParamIfPresent("value", Optional.ofNullable(request.getValue()))
                    .queryParamIfPresent("transport", Optional.ofNullable(request.getTransport()))
                    .queryParamIfPresent("deliver_option", Optional.ofNullable(request.getDeliverOption()))
                    .build()
                    .toUriString();

            log.info("Gọi GHTK API: {}", url);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                if (Boolean.TRUE.equals(body.get("success"))) {
                    Map<String, Object> fee = (Map<String, Object>) body.get("fee");
                    
                    Integer shippingFee = parseInteger(fee.get("fee"));
                    Integer insuranceFee = parseInteger(fee.get("insurance_fee"));
                    
                    return ShippingResponse.builder()
                            .success(true)
                            .message("Tính phí thành công")
                            .fee(shippingFee)
                            .insuranceFee(insuranceFee)
                            .totalFee(shippingFee + insuranceFee)
                            .deliveryTime((String) fee.get("delivery_time"))
                            .expectedDelivery((String) fee.get("expected_delivery"))
                            .extFee(Boolean.TRUE.equals(fee.get("extFee")))
                            .shipMoneyLead((String) fee.get("ship_fee_only"))
                            .build();
                } else {
                    return ShippingResponse.builder()
                            .success(false)
                            .message((String) body.get("message"))
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi GHTK API tính phí: {}", e.getMessage());
        }
        
        return calculateMockFee(request);
    }

    // ==================== MOCK DATA (Khi không có Token) ====================

    /**
     * Mock danh sách 63 Tỉnh/Thành phố Việt Nam
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
     * Mock danh sách Quận/Huyện (sample)
     */
    private List<DistrictDTO> getMockDistricts(Integer provinceId) {
        // Mock một số quận/huyện mẫu cho Hà Nội và HCM
        if (provinceId == 1) { // Hà Nội
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
        } else if (provinceId == 2) { // HCM
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
        
        // Trả về mock chung cho các tỉnh khác
        return Arrays.asList(
                new DistrictDTO(100 + provinceId, "Thành phố/Thị xã " + provinceId, provinceId),
                new DistrictDTO(200 + provinceId, "Huyện A", provinceId),
                new DistrictDTO(300 + provinceId, "Huyện B", provinceId)
        );
    }

    /**
     * Mock danh sách Phường/Xã (sample)
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
     * Mock tính phí vận chuyển khi không có Token
     */
    private ShippingResponse calculateMockFee(ShippingRequest request) {
        // Tính phí mock dựa trên cân nặng và khoảng cách
        int baseFee = 15000; // Phí cơ bản
        int weightFee = (request.getWeight() / 500) * 5000; // Thêm 5k mỗi 500g
        
        // Nếu khác tỉnh, thêm phí
        boolean sameProv = request.getPickProvince() != null 
                && request.getPickProvince().equals(request.getProvince());
        int distanceFee = sameProv ? 0 : 20000;
        
        int totalShippingFee = baseFee + weightFee + distanceFee;
        
        // Phí bảo hiểm = 0.5% giá trị đơn hàng
        int insuranceFee = 0;
        if (request.getValue() != null && request.getValue() > 0) {
            insuranceFee = (int) (request.getValue() * 0.005);
        }
        
        return ShippingResponse.builder()
                .success(true)
                .message("Tính phí thành công (Mock Data)")
                .fee(totalShippingFee)
                .insuranceFee(insuranceFee)
                .totalFee(totalShippingFee + insuranceFee)
                .deliveryTime(sameProv ? "1-2 ngày" : "3-5 ngày")
                .expectedDelivery("05/02/2026")
                .extFee(false)
                .shipMoneyLead("Trong ngày")
                .build();
    }

    /**
     * Helper: Parse Integer từ Object
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
