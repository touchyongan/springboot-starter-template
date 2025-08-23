package io.touchyongan.starter_template.common.validation.validator;

import io.touchyongan.starter_template.common.util.AppUtil;
import io.touchyongan.starter_template.common.validation.constraint.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidDataValidator implements ConstraintValidator<ValidDate, String> {
    private boolean optional;
    private DateTimeFormatter formatter;

    @Override
    public void initialize(final ValidDate constraint) {
        this.optional = constraint.optional();
        this.formatter = DateTimeFormatter.ofPattern(constraint.pattern());
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext context) {
        if (AppUtil.isBlank(value)) {
            return optional; // allow empty if optional=true
        }
        try {
            LocalDate.parse(value, formatter);
            return true;
        } catch (final DateTimeParseException e) {
            return false;
        }
    }
}
