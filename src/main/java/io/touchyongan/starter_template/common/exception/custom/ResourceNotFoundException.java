package io.touchyongan.starter_template.common.exception.custom;

import io.touchyongan.starter_template.common.exception.custom.impl.ResourceNotFoundError;

public class ResourceNotFoundException extends BaseApiException {

    public ResourceNotFoundException(final Object... args) {
        super(ResourceNotFoundError.NOT_FOUND, args);
    }

}
