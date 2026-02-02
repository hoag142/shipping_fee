package com.example.shipping_fee.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shipping_fee.dto.ApiResponse;
import com.example.shipping_fee.dto.DistrictDTO;
import com.example.shipping_fee.dto.ProvinceDTO;
import com.example.shipping_fee.dto.ShippingRequest;
import com.example.shipping_fee.dto.ShippingResponse;
import com.example.shipping_fee.dto.WardDTO;
import com.example.shipping_fee.service.GhtkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Shipping API
 * Base URL: /api/shipping
 * 
 * Endpoints:
 * - GET  /provinces              : Lấy danh sách Tỉnh/Thành
 * - GET  /districts/{provinceId} : Lấy danh sách Quận/Huyện theo Tỉnh
 * - GET  /wards/{districtId}     : Lấy danh sách Phường/Xã theo Huyện
 * - POST /calculate              : Tính phí vận chuyển
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Cho phép React gọi từ localhost:3000
public class ShippingController {

    private final GhtkService ghtkService;

    // ==================== LẤY DANH SÁCH ĐỊA CHỈ ====================

    /**
     * GET /api/shipping/provinces
     * Lấy danh sách 63 Tỉnh/Thành phố để đổ vào dropdown
     * 
     * React gọi lúc mới vào trang (Initial Load)
     */
    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceDTO>>> getProvinces() {
        log.info("API được gọi: GET /api/shipping/provinces");
        
        List<ProvinceDTO> provinces = ghtkService.getProvinces();
        
        return ResponseEntity.ok(ApiResponse.success(provinces, 
                "Lấy danh sách " + provinces.size() + " tỉnh/thành phố thành công"));
    }

    /**
     * GET /api/shipping/districts/{provinceId}
     * Lấy danh sách Quận/Huyện theo Tỉnh
     * 
     * React gọi khi user chọn Tỉnh từ dropdown
     */
    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<ApiResponse<List<DistrictDTO>>> getDistricts(
            @PathVariable Integer provinceId) {
        log.info("API được gọi: GET /api/shipping/districts/{}", provinceId);
        
        List<DistrictDTO> districts = ghtkService.getDistricts(provinceId);
        
        return ResponseEntity.ok(ApiResponse.success(districts,
                "Lấy danh sách " + districts.size() + " quận/huyện thành công"));
    }

    /**
     * GET /api/shipping/wards/{districtId}
     * Lấy danh sách Phường/Xã theo Quận/Huyện
     * 
     * React gọi khi user chọn Quận/Huyện từ dropdown
     */
    @GetMapping("/wards/{districtId}")
    public ResponseEntity<ApiResponse<List<WardDTO>>> getWards(
            @PathVariable Integer districtId) {
        log.info("API được gọi: GET /api/shipping/wards/{}", districtId);
        
        List<WardDTO> wards = ghtkService.getWards(districtId);
        
        return ResponseEntity.ok(ApiResponse.success(wards,
                "Lấy danh sách " + wards.size() + " phường/xã thành công"));
    }

    // ==================== TÍNH PHÍ VẬN CHUYỂN ====================

    /**
     * POST /api/shipping/calculate
     * Tính phí vận chuyển dựa trên địa chỉ gửi, nhận và cân nặng
     * 
     * React gọi khi user bấm nút "Tính phí"
     * 
     * Request Body (ShippingRequest):
     * {
     *   "pickProvince": "Hà Nội",
     *   "pickDistrict": "Quận Cầu Giấy",
     *   "province": "Hồ Chí Minh",
     *   "district": "Quận 1",
     *   "weight": 2000,
     *   "value": 500000
     * }
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ShippingResponse>> calculateFee(
            @RequestBody ShippingRequest request) {
        log.info("API được gọi: POST /api/shipping/calculate");
        log.info("Request: {} -> {}, {}g", 
                request.getPickProvince(), request.getProvince(), request.getWeight());
        
        // Validate input cơ bản
        if (request.getPickProvince() == null || request.getPickProvince().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn Tỉnh/Thành gửi hàng"));
        }
        if (request.getPickDistrict() == null || request.getPickDistrict().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn Quận/Huyện gửi hàng"));
        }
        if (request.getProvince() == null || request.getProvince().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn Tỉnh/Thành nhận hàng"));
        }
        if (request.getDistrict() == null || request.getDistrict().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn Quận/Huyện nhận hàng"));
        }
        if (request.getWeight() == null || request.getWeight() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng nhập cân nặng hợp lệ (gram)"));
        }
        
        ShippingResponse result = ghtkService.calculateFee(request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result, "Tính phí thành công"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
        }
    }

    // ==================== HEALTH CHECK ====================

    /**
     * GET /api/shipping/health
     * Kiểm tra API có hoạt động không
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK", "API đang hoạt động bình thường"));
    }
}
