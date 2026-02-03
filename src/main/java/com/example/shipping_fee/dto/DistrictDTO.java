package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO District information (level 2)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDTO {
    
    // District ID
    private Integer id;

    // District Name
    private String name;

    // Province ID
    private Integer provinceId;
    
}
