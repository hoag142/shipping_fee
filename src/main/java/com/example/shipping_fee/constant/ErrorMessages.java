package com.example.shipping_fee.constant;

/**
 * Constants for error messages used in validation
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Prevent instantiation
    }

    // Field names
    public static final String FIELD_FROM_DISTRICT_ID = "fromDistrictId";
    public static final String FIELD_FROM_WARD_CODE = "fromWardCode";
    public static final String FIELD_TO_DISTRICT_ID = "toDistrictId";
    public static final String FIELD_TO_WARD_CODE = "toWardCode";
    public static final String FIELD_WEIGHT = "weight";
    public static final String FIELD_SERVICE_TYPE_ID = "serviceTypeId";
    public static final String FIELD_INSURANCE_VALUE = "insuranceValue";
    public static final String FIELD_COD_VALUE = "codValue";
    public static final String FIELD_LENGTH = "length";
    public static final String FIELD_WIDTH = "width";
    public static final String FIELD_HEIGHT = "height";

    // Validation error messages
    public static final String ERR_FROM_DISTRICT_REQUIRED = "Pickup district is required";
    public static final String ERR_FROM_WARD_REQUIRED = "Pickup ward is required";
    public static final String ERR_TO_DISTRICT_REQUIRED = "Delivery district is required";
    public static final String ERR_TO_WARD_REQUIRED = "Delivery ward is required";
    public static final String ERR_WEIGHT_REQUIRED = "Weight is required";
    public static final String ERR_WEIGHT_INVALID = "Weight must be greater than 0";
    public static final String ERR_WEIGHT_MAX_EXCEEDED = "Weight cannot exceed 50000g (50kg)";
    public static final String ERR_SERVICE_TYPE_INVALID = "Service type must be 1 (Express), 2 (Standard), or 3 (Economy)";
    public static final String ERR_INSURANCE_VALUE_NEGATIVE = "Insurance value cannot be negative";
    public static final String ERR_INSURANCE_VALUE_MAX_EXCEEDED = "Insurance value cannot exceed 5,000,000 VND";
    public static final String ERR_COD_VALUE_NEGATIVE = "COD value cannot be negative";
    public static final String ERR_COD_VALUE_MAX_EXCEEDED = "COD value cannot exceed 5,000,000 VND";
    public static final String ERR_DIMENSION_NEGATIVE = "Dimension values cannot be negative";
    public static final String ERR_DIMENSION_MAX_EXCEEDED = "Dimension cannot exceed 200cm";

    // Success messages
    public static final String MSG_FEE_CALCULATION_SUCCESS = "Fee calculation successful";
    public static final String MSG_FEE_CALCULATION_SUCCESS_MOCK = "Fee calculation successful (Mock Data)";

    // General error messages
    public static final String ERR_VALIDATION_FAILED = "Validation failed";
    public static final String ERR_API_CALL_FAILED = "Failed to call GHN API";
}
