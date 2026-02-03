package com.example.shipping_fee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Input - Nhận dữ liệu từ React gửi lên để tính phí ship qua GHN API
 * Docs: https://api.ghn.vn/home/docs/detail?id=76
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRequest {

    // === Địa chỉ lấy hàng (Người gửi) - Optional, tự động lấy từ shop nếu không truyền ===
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;       // ID Quận/Huyện gửi

    @JsonProperty("from_ward_code")
    private String fromWardCode;          // Mã Phường/Xã gửi

    // === Địa chỉ giao hàng (Người nhận) - Required ===
    @JsonProperty("to_district_id")
    private Integer toDistrictId;         // ID Quận/Huyện nhận (bắt buộc)

    @JsonProperty("to_ward_code")
    private String toWardCode;            // Mã Phường/Xã nhận (bắt buộc)

    // === Thông tin dịch vụ ===
    @JsonProperty("service_id")
    private Integer serviceId;            // ID dịch vụ (lấy từ Service API)

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;        // Loại dịch vụ: 1=Nhanh, 2=Chuẩn, 3=Tiết kiệm

    // === Thông tin hàng hóa ===
    private Integer weight;               // Cân nặng (gram)
    private Integer length;               // Chiều dài (cm)
    private Integer width;                // Chiều rộng (cm)
    private Integer height;               // Chiều cao (cm)

    // === Giá trị & Phí ===
    @JsonProperty("insurance_value")
    private Integer insuranceValue;       // Giá trị hàng hóa để tính bảo hiểm (VND), max 5,000,000

    @JsonProperty("cod_value")
    private Integer codValue;             // Số tiền thu hộ COD (VND), max 5,000,000

    @JsonProperty("cod_failed_amount")
    private Integer codFailedAmount;      // Số tiền thu khi giao thất bại

    // === Khuyến mãi ===
    private String coupon;                // Mã giảm giá

}
