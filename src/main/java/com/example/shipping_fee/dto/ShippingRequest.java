package com.example.shipping_fee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Input - Request data from React to calculate shipping fee via GHN API
 * Docs: https://api.ghn.vn/home/docs/detail?id=76
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRequest {

    // Pickup address (Sender) - Optional, auto-fetched from shop if not provided
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;       // Sender District ID

    @JsonProperty("from_ward_code")
    private String fromWardCode;          // Sender Ward Code

    // Delivery address (Receiver) - Required
    @JsonProperty("to_district_id")
    private Integer toDistrictId;         // Receiver District ID (required)

    @JsonProperty("to_ward_code")
    private String toWardCode;            // Receiver Ward Code (required)

    // Service information
    @JsonProperty("service_id")
    private Integer serviceId;            // Service ID (from Service API)

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;        // Service type: 1=Express, 2=Standard, 3=Economy

    // Package information
    private Integer weight;               // Weight (gram)
    private Integer length;               // Length (cm)
    private Integer width;                // Width (cm)
    private Integer height;               // Height (cm)

    // Value and fees
    @JsonProperty("insurance_value")
    private Integer insuranceValue;       // Goods value for insurance (VND), max 5,000,000

    @JsonProperty("cod_value")
    private Integer codValue;             // COD amount (VND), max 5,000,000

    @JsonProperty("cod_failed_amount")
    private Integer codFailedAmount;      // Amount to collect on failed delivery

    // Promotion
    private String coupon;                // Discount code

}
