package io.touchyongan.starter_template.common.util;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditChange {

    /**
     * Map dto field to entity field
     */
    String entityField() default "";

    /**
     * Ignore update null or empty value.
     * Some use-case, the null value is mean not provide in api for update
     */
    boolean ignoreUpdateNullValue() default false;

    /**
     * Some use-case dto field not for update entity field
     */
    boolean ignoreField() default false;
}
