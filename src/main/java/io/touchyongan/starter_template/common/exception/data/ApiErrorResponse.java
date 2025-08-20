package io.touchyongan.starter_template.common.exception.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.touchyongan.starter_template.common.exception.custom.BaseApiException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;

import java.util.Locale;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse extends ProblemDetail {
    private String errorCode;
    private String message;

    public ApiErrorResponse(final int rawStatusCode) {
        super(rawStatusCode);
    }

    public static ApiErrorResponse getInstance(final int rawStatusCode,
                                               final BaseApiException e,
                                               final MessageSource messageSource,
                                               final Locale locale) {
        final var message = messageSource.getMessage(e.getMessageKey(), e.getArgs(), locale);
        final var errorResp = new ApiErrorResponse(rawStatusCode);
        errorResp.setErrorCode(e.getErrorCode());
        errorResp.setMessage(message);
        return errorResp;
    }
}
