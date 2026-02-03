package com.example.shipping_fee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Output - Kết quả tính phí ship từ GHN API
 * Docs: https://api.ghn.vn/home/docs/detail?id=76
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {

    private boolean success;          // Trạng thái: true = thành công, false = lỗi
    private String message;           // Thông báo (nếu có lỗi)

    // === Chi tiết phí từ GHN API ===
    private Integer total;            // Tổng phí dịch vụ (VND)

    @JsonProperty("service_fee")
    private Integer serviceFee;       // Phí dịch vụ cơ bản (VND)

    @JsonProperty("insurance_fee")
    private Integer insuranceFee;     // Phí bảo hiểm (VND)

    @JsonProperty("pick_station_fee")
    private Integer pickStationFee;   // Phí lấy hàng tại bưu cục (VND)

    @JsonProperty("coupon_value")
    private Integer couponValue;      // Giá trị giảm giá từ coupon (VND)

    @JsonProperty("r2s_fee")
    private Integer r2sFee;           // Phí giao lại (VND)

    @JsonProperty("document_return")
    private Integer documentReturn;   // Phí trả chứng từ (VND)

    @JsonProperty("double_check")
    private Integer doubleCheck;      // Phí kiểm tra hàng (VND)

    @JsonProperty("cod_fee")
    private Integer codFee;           // Phí thu hộ COD (VND)

    @JsonProperty("pick_remote_areas_fee")
    private Integer pickRemoteAreasFee;    // Phụ phí lấy hàng vùng xa (VND)

    @JsonProperty("deliver_remote_areas_fee")
    private Integer deliverRemoteAreasFee; // Phụ phí giao hàng vùng xa (VND)

    @JsonProperty("cod_failed_fee")
    private Integer codFailedFee;     // Phí khi thu COD thất bại (VND)

}
