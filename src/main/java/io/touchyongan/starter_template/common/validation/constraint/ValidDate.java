package io.touchyongan.starter_template.common.validation.constraint;

import io.touchyongan.starter_template.common.validation.validator.ValidDataValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDataValidator.class)
public @interface ValidDate {
    String message() default "custom.validation.date.invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Note: The order of placeholder position in i18n will be based on asc of property name field.
    // e.g.: custom.validation.date.invalid="{0}" Invalid date format, expected "{2}".
    // Assume property name `startDate` then the message will be:
    // "startDate" Invalid date format, expected "yyyy-MM-dd".

    /** Whether null/blank values are allowed */
    boolean optional() default true;

    /** Expected date format (default yyyy-MM-dd) */
    String pattern() default "yyyy-MM-dd";
}
