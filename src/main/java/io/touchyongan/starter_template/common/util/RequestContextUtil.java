package io.touchyongan.starter_template.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestContextUtil {
    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static final String KEY_OLD_VALUE = "oldValue";
    public static final String KEY_NEW_VALUE = "newValue";
    public static final String KEY_RESOURCE_ID = "resourceId";

    private RequestContextUtil() {
    }

    public static void put(final String key,
                           final Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object get(final String key) {
        return CONTEXT.get().get(key);
    }

    public static void clear() {
        CONTEXT.get().clear();
    }

    public static String getIPAddress(final HttpServletRequest request) {
        final var ip = request.getHeader("x-forwarded-for");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    public static String getDeviceInfo(final HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
