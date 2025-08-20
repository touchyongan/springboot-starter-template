package io.touchyongan.starter_template.feature.audit.converter;

import io.touchyongan.starter_template.feature.audit.entity.ActionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Optional;

@Converter(autoApply = true)
public class ActionStatusConverter implements AttributeConverter<ActionStatus, String> {

    @Override
    public String convertToDatabaseColumn(final ActionStatus actionStatus) {
        return Optional.ofNullable(actionStatus)
                .map(ActionStatus::getStatus)
                .orElse(null);
    }

    @Override
    public ActionStatus convertToEntityAttribute(final String s) {
        return Optional.ofNullable(s)
                .map(ActionStatus::from)
                .orElse(null);
    }
}
