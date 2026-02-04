package com.example.shipping_fee.constant;

/**
 * Constants for GHN API Endpoints.
 * Tập trung quản lý tất cả endpoints của GHN API tại đây.
 * Khi GHN thay đổi API, chỉ cần sửa file này.
 * 
 * @see <a href="https://api.ghn.vn/home/docs/detail">GHN API Documentation</a>
 */
public final class GhnApiEndpoints {

    private GhnApiEndpoints() {
        // Prevent instantiation
    }

    // Base URLs
    
    // Production environment
    public static final String BASE_URL_PRODUCTION = "https://online-gateway.ghn.vn";
    
    // Development/Sandbox environment
    public static final String BASE_URL_DEVELOPMENT = "https://dev-online-gateway.ghn.vn";

    // Master Data APIs (GET)
    
    // Get list of provinces/cities - Method: GET, Headers: Token
    public static final String PROVINCE = "/shiip/public-api/master-data/province";
    
    // Get list of districts by province - Method: GET, Headers: Token, Query: province_id
    public static final String DISTRICT = "/shiip/public-api/master-data/district";
    
    // Get list of wards by district - Method: GET, Headers: Token, Query: district_id
    public static final String WARD = "/shiip/public-api/master-data/ward";

    // Shipping Order APIs (POST)
    
    // Calculate shipping fee - Method: POST, Headers: Token, ShopId
    public static final String CALCULATE_FEE = "/shiip/public-api/v2/shipping-order/fee";
    
    // Get available shipping services - Method: POST, Headers: Token, ShopId
    public static final String AVAILABLE_SERVICES = "/shiip/public-api/v2/shipping-order/available-services";
    
    // Create shipping order - Method: POST, Headers: Token, ShopId
    public static final String CREATE_ORDER = "/shiip/public-api/v2/shipping-order/create";
    
    // Get order detail - Method: POST, Headers: Token
    public static final String ORDER_DETAIL = "/shiip/public-api/v2/shipping-order/detail";
    
    // Cancel order - Method: POST, Headers: Token, ShopId
    public static final String CANCEL_ORDER = "/shiip/public-api/v2/switch-status/cancel";

    // Helper methods to build full URLs
    
    /**
     * Build full URL for province endpoint.
     * 
     * @param baseUrl the base URL (production or development)
     * @return full URL for province API
     */
    public static String getProvinceUrl(String baseUrl) {
        return baseUrl + PROVINCE;
    }
    
    /**
     * Build full URL for district endpoint with province_id.
     * 
     * @param baseUrl the base URL (production or development)
     * @param provinceId the province ID to filter districts
     * @return full URL for district API with query parameter
     */
    public static String getDistrictUrl(String baseUrl, Integer provinceId) {
        return baseUrl + DISTRICT + "?province_id=" + provinceId;
    }
    
    /**
     * Build full URL for ward endpoint with district_id.
     * 
     * @param baseUrl the base URL (production or development)
     * @param districtId the district ID to filter wards
     * @return full URL for ward API with query parameter
     */
    public static String getWardUrl(String baseUrl, Integer districtId) {
        return baseUrl + WARD + "?district_id=" + districtId;
    }
    
    /**
     * Build full URL for calculate fee endpoint.
     * 
     * @param baseUrl the base URL (production or development)
     * @return full URL for calculate fee API
     */
    public static String getCalculateFeeUrl(String baseUrl) {
        return baseUrl + CALCULATE_FEE;
    }
    
    /**
     * Build full URL for available services endpoint.
     * 
     * @param baseUrl the base URL (production or development)
     * @return full URL for available services API
     */
    public static String getAvailableServicesUrl(String baseUrl) {
        return baseUrl + AVAILABLE_SERVICES;
    }
}
