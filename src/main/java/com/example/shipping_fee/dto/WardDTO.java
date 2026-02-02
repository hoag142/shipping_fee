package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin Phường/Xã
 * Dùng để đổ vào dropdown chọn địa chỉ cấp 3 (cấp 4 sau sát nhập)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardDTO {
    
    private Integer id;
    private String name;
    private Integer districtId;
    
}
