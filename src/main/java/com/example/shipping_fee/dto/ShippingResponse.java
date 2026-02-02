package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Output - Kết quả tính phí ship trả về cho React
 * Chứa: phí ship, thời gian giao hàng dự kiến, thông tin bổ sung
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {
    
    private boolean success;          // Trạng thái: true = thành công, false = lỗi
    private String message;           // Thông báo (nếu có lỗi)
    
    // === Kết quả tính phí ===
    private Integer fee;              // Phí vận chuyển (VND)
    private Integer insuranceFee;     // Phí bảo hiểm (VND)
    private Integer totalFee;         // Tổng phí = fee + insuranceFee
    
    // === Thời gian giao hàng ===
    private String deliveryTime;      // Thời gian giao hàng dự kiến (ví dụ: "2-3 ngày")
    private String expectedDelivery;  // Ngày giao dự kiến (ví dụ: "03/02/2026")
    
    // === Thông tin bổ sung ===
    private boolean extFee;           // Có phụ phí vùng xa không
    private String shipMoneyLead;     // Thời gian thu tiền COD
    
}
