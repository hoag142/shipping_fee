package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin Quận/Huyện
 * Dùng để đổ vào dropdown chọn địa chỉ cấp 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDTO {
    
    private Integer id;
    private String name;
    private Integer provinceId;
    
}
