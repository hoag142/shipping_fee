package com.example.shipping_fee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO Output - Shipping fee calculation result from GHN API
 * Docs: https://api.ghn.vn/home/docs/detail?id=76
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {

    private boolean success;          // Status: true = success, false = error
    private String message;           // Message (if error)
    private List<ErrorMessageDTO> errors;  // List of field-level errors

    // Fee details from GHN API
    private Integer total;            // Total service fee (VND)

    @JsonProperty("service_fee")
    private Integer serviceFee;       // Base service fee (VND)

    @JsonProperty("insurance_fee")
    private Integer insuranceFee;     // Insurance fee (VND)

    @JsonProperty("pick_station_fee")
    private Integer pickStationFee;   // Station pickup fee (VND)

    @JsonProperty("coupon_value")
    private Integer couponValue;      // Coupon discount value (VND)

    @JsonProperty("r2s_fee")
    private Integer r2sFee;           // Return to sender fee (VND)

    @JsonProperty("document_return")
    private Integer documentReturn;   // Document return fee (VND)

    @JsonProperty("double_check")
    private Integer doubleCheck;      // Double check fee (VND)

    @JsonProperty("cod_fee")
    private Integer codFee;           // COD collection fee (VND)

    @JsonProperty("pick_remote_areas_fee")
    private Integer pickRemoteAreasFee;    // Remote area pickup surcharge (VND)

    @JsonProperty("deliver_remote_areas_fee")
    private Integer deliverRemoteAreasFee; // Remote area delivery surcharge (VND)

    @JsonProperty("cod_failed_fee")
    private Integer codFailedFee;     // Failed COD collection fee (VND)

}
