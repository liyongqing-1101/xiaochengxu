package com.wxjiaozi.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the current user's ID or openid into a controller method parameter.
 * <p>
 * Usage:
 * <pre>{@code
 *   @GetMapping("/profile")
 *   public Result<?> profile(@CurrentUser Long userId) { ... }
 *
 *   @GetMapping("/info")
 *   public Result<?> info(@CurrentUser("openid") String openid) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {

    /**
     * The attribute to extract from the request.
     * {@code "id"} returns {@code currentUserId} as Long,
     * {@code "openid"} returns {@code currentOpenid} as String,
     * {@code "adminId"} returns {@code currentAdminId} as Long.
     */
    String value() default "id";

    /**
     * Whether the current user is required. When false, returns null for unauthenticated requests.
     */
    boolean required() default true;
}
