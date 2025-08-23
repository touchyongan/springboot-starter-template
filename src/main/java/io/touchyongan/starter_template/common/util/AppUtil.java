package io.touchyongan.starter_template.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

public final class AppUtil {

    private AppUtil() {
    }

    public static boolean isNotBlank(final String s) {
        return StringUtils.hasText(s);
    }

    public static boolean isBlank(final String s) {
        return !isNotBlank(s);
    }

    public static boolean hasChanged(final Object o1,
                                     final Object o2) {
        return !Objects.equals(o1, o2);
    }

    public static boolean hasChanged(final Collection<?> o1,
                                     final Collection<?> o2) {

        return CollectionUtils.isEqualCollection(o1, o2);
    }
}
