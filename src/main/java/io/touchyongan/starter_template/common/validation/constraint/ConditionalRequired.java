package io.touchyongan.starter_template.common.validation.constraint;

import io.touchyongan.starter_template.common.validation.validator.ConditionalRequiredValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalRequiredValidator.class)
public @interface ConditionalRequired {

    String message() default "custom.validation.conditional.required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Field that triggers condition */
    String field();

    /** Value that triggers requirement */
    String expectedValue();

    /** Field that becomes required */
    String requiredField();
}
