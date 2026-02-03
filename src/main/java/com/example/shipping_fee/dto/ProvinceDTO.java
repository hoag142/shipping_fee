package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO information Province (level 1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDTO {

    // Province ID 
    private Integer provinceId;

    // Province Name
    private String provinceName;

    // Province Code
    private String code;
}
