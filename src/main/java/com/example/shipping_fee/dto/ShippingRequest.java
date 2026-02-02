package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Input - Nhận dữ liệu từ React gửi lên để tính phí ship
 * Chứa thông tin: địa chỉ gửi, địa chỉ nhận, cân nặng, giá trị đơn hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRequest {
    
    // === Địa chỉ lấy hàng (Người gửi) ===
    private String pickProvince;      // Tỉnh/Thành phố gửi
    private String pickDistrict;      // Quận/Huyện gửi
    private String pickWard;          // Phường/Xã gửi (nếu có)
    private String pickAddress;       // Địa chỉ chi tiết gửi (nếu có)
    
    // === Địa chỉ giao hàng (Người nhận) ===
    private String province;          // Tỉnh/Thành phố nhận
    private String district;          // Quận/Huyện nhận
    private String ward;              // Phường/Xã nhận (nếu có)
    private String address;           // Địa chỉ chi tiết nhận (nếu có)
    
    // === Thông tin hàng hóa ===
    private Integer weight;           // Cân nặng (gram)
    private Integer value;            // Giá trị đơn hàng (VND) - để tính phí bảo hiểm
    
    // === Tùy chọn vận chuyển ===
    private String transport;         // Loại vận chuyển: "road" (đường bộ) hoặc "fly" (đường bay)
    private String deliverOption;     // Tùy chọn giao: "xteam" (giao ngay) hoặc "none"
    
}
