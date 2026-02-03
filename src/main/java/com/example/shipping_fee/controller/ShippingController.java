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
 * REST Controller for Shipping API
 * Base URL: /api/shipping
 *
 * Endpoints:
 * - GET  /provinces              : Get list of Provinces/Cities
 * - GET  /districts/{provinceId} : Get list of Districts by Province
 * - GET  /wards/{districtId}     : Get list of Wards by District
 * - POST /calculate              : Calculate shipping fee
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow React calls from localhost:3000
public class ShippingController {

    private final GhtkService ghtkService;

    /**
     * GET /api/shipping/provinces
     * Get list of 63 Provinces/Cities for dropdown
     *
     * Called by React on initial page load
     */
    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceDTO>>> getProvinces() {
        log.info("API called: GET /api/shipping/provinces");

        List<ProvinceDTO> provinces = ghtkService.getProvinces();

        return ResponseEntity.ok(ApiResponse.success(provinces,
                "Successfully retrieved " + provinces.size() + " provinces/cities"));
    }

    /**
     * GET /api/shipping/districts/{provinceId}
     * Get list of Districts by Province
     *
     * Called by React when user selects a Province from dropdown
     */
    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<ApiResponse<List<DistrictDTO>>> getDistricts(
            @PathVariable Integer provinceId) {
        log.info("API called: GET /api/shipping/districts/{}", provinceId);

        List<DistrictDTO> districts = ghtkService.getDistricts(provinceId);

        return ResponseEntity.ok(ApiResponse.success(districts,
                "Successfully retrieved " + districts.size() + " districts"));
    }

    /**
     * GET /api/shipping/wards/{districtId}
     * Get list of Wards by District
     *
     * Called by React when user selects a District from dropdown
     */
    @GetMapping("/wards/{districtId}")
    public ResponseEntity<ApiResponse<List<WardDTO>>> getWards(
            @PathVariable Integer districtId) {
        log.info("API called: GET /api/shipping/wards/{}", districtId);

        List<WardDTO> wards = ghtkService.getWards(districtId);

        return ResponseEntity.ok(ApiResponse.success(wards,
                "Successfully retrieved " + wards.size() + " wards"));
    }

    /**
     * POST /api/shipping/calculate
     * Calculate shipping fee based on delivery address and weight via GHN API
     *
     * Called by React when user clicks "Calculate" button
     *
     * Request Body (ShippingRequest):
     * {
     *   "toDistrictId": 1442,
     *   "toWardCode": "20314",
     *   "weight": 1000,
     *   "serviceTypeId": 2,
     *   "insuranceValue": 500000
     * }
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ShippingResponse>> calculateFee(
            @RequestBody ShippingRequest request) {
        log.info("API called: POST /api/shipping/calculate");
        log.info("Request: from_district={} -> to_district={}, to_ward={}, {}g",
                request.getFromDistrictId(), request.getToDistrictId(),
                request.getToWardCode(), request.getWeight());

        // Validate required fields
        if (request.getToDistrictId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select delivery District"));
        }
        if (request.getToWardCode() == null || request.getToWardCode().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select delivery Ward"));
        }
        if (request.getWeight() == null || request.getWeight() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please enter a valid weight (gram)"));
        }

        ShippingResponse result = ghtkService.calculateFee(request);

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result, "Fee calculation successful"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
        }
    }

    /**
     * GET /api/shipping/health
     * Check if API is running
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK", "API is running normally"));
    }
}
