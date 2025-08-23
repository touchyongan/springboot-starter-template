package io.touchyongan.starter_template.common.validation.validator;

import io.touchyongan.starter_template.common.validation.constraint.ConditionalRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class ConditionalRequiredValidator implements ConstraintValidator<ConditionalRequired, Object> {
    private String field;
    private String expectedValue;
    private String requiredField;

    @Override
    public void initialize(final ConditionalRequired constraint) {
        this.field = constraint.field();
        this.expectedValue = constraint.expectedValue();
        this.requiredField = constraint.requiredField();
    }

    @Override
    public boolean isValid(final Object obj,
                           final ConstraintValidatorContext context) {
        final var beanWrapper = new BeanWrapperImpl(obj);

        final var fieldValue = beanWrapper.getPropertyValue(field);
        final var requiredValue = beanWrapper.getPropertyValue(requiredField);

        if (Objects.nonNull(fieldValue) && expectedValue.equals(fieldValue.toString())) {
            final var valid = Objects.nonNull(requiredValue) && !requiredValue.toString().isBlank();
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(requiredField)
                        .addConstraintViolation();
            }
            return valid;
        }
        return true; // no condition triggered
    }
}
