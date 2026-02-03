package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Ward information (level 3)
 * Used to populate dropdown for selecting level 3 address
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardDTO {

    // Ward Code (GHN API returns WardCode as String, not Integer)
    private String wardCode;

    // Ward Name
    private String name;

    // District ID
    private Integer districtId;

}
