package com.clickstechnology.Brillo.Mall.infrastructure.caching;

public final class CacheNames {
    public static final String USER_AUTH = "cache_user_auth_";
    public static final String USER_USER = "cache_user_";
    public static final String OTP = "cache_otp_";
    public static final String OTP_CONTINUE = "cache_otp_continue_";
    public static final String AUTH_USER_TOKEN = "auth_user_token_";
    public static final String BLACKLISTED_TOKENS = "blacklisted_tokens_";
    public static final String REQUEST_LOG_QUEUE = "_request_log_";
    public static final String BUSINESS_REFERENCE = "_business_reference_";
    public static final String BUSINESS_SLUG = "_business_slug_";
    public static final String ORDER_ID = "_order_id_";

    private CacheNames() {}
}