package com.example.shipping_fee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for field-level error messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorMessageDTO {

    private String field;      // Field name that has error
    private String message;    // Error message for this field

}
