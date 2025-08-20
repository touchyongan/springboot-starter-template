package io.touchyongan.starter_template.common.exception.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.touchyongan.starter_template.common.exception.custom.BaseApiException;
import io.touchyongan.starter_template.common.exception.custom.InputValidationException;
import io.touchyongan.starter_template.common.exception.custom.ResourceNotFoundException;
import io.touchyongan.starter_template.common.exception.custom.impl.AuthError;
import io.touchyongan.starter_template.common.exception.custom.impl.InputValidationError;
import io.touchyongan.starter_template.common.exception.data.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionController {
    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInputValidationException(final MethodArgumentNotValidException e) {
        final var locale = getLocale();
        final var errorResp = new ApiErrorResponse(400);
        final var errors = new ArrayList<Map<String, Object>>();

        for (final var fe : e.getBindingResult().getFieldErrors()) {
            final var message = messageSource.getMessage(fe.getDefaultMessage(), fe.getArguments(), locale);
            final var errorCode = Optional.ofNullable(InputValidationError.fromMessageKey(fe.getDefaultMessage()))
                    .map(InputValidationError::getErrorCode)
                    .orElse(null);

            final var fieldError = new LinkedHashMap<String, Object>();
            fieldError.put("field", fe.getField());
            fieldError.put("value", fe.getRejectedValue());
            fieldError.put("errorCode", errorCode);
            fieldError.put("message", message);
            errors.add(fieldError);
        }
        errorResp.setProperty("errors", errors);
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(InputValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleInputValidationException(final InputValidationException e) {
        final var errorResp = createCommonError(400, e);
        final var errors = new ArrayList<Map<String, Object>>();
        final var locale = getLocale();
        for (final var fe : e.getErrors()) {
            final var message = messageSource.getMessage(fe.messageKey(), fe.args(), locale);
            final var fieldError = new LinkedHashMap<String, Object>();
            fieldError.put("field", fe.field());
            fieldError.put("value", fe.value());
            fieldError.put("errorCode", fe.errorCode());
            fieldError.put("message", message);
            errors.add(fieldError);
        }
        errorResp.setProperty("errors", errors);
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(final ResourceNotFoundException e) {
        final var errorResp = createCommonError(404, e);
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseApiException(final BaseApiException e) {
        final var errorResp = createCommonError(422, e);
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(final AuthenticationException e) {
        final var locale = getLocale();
        final var authError = AuthError.AUTH_UNAUTHENTICATED;
        final var message = messageSource.getMessage(authError.getMessageKey(), new Object[]{}, locale);

        final var errorResp = new ApiErrorResponse(401);
        errorResp.setErrorCode(authError.getErrorCode());
        errorResp.setMessage(message);
        errorResp.setDetail(e.getMessage());
        errorResp.setProperty("traceId", MDC.get("UUID"));

        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(final AccessDeniedException e) {
        final var locale = getLocale();
        final var authError = AuthError.AUTH_UNAUTHORIZED;
        final var message = messageSource.getMessage(authError.getMessageKey(), new Object[]{}, locale);

        final var errorResp = new ApiErrorResponse(403);
        errorResp.setErrorCode(authError.getErrorCode());
        errorResp.setMessage(message);
        errorResp.setDetail(e.getMessage());
        errorResp.setProperty("traceId", MDC.get("UUID"));
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(value = {SignatureException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidJwtSignatureException(final SignatureException e) {
        final var locale = getLocale();
        final var authError = AuthError.AUTH_TOKEN_INVALID;
        final var message = messageSource.getMessage(authError.getMessageKey(), new Object[]{}, locale);

        final var errorResp = new ApiErrorResponse(400);
        errorResp.setErrorCode(authError.getErrorCode());
        errorResp.setMessage(message);
        errorResp.setDetail(e.getMessage());
        errorResp.setProperty("traceId", MDC.get("UUID"));
        return ResponseEntity.ok(errorResp);
    }

    @ExceptionHandler(value = {ExpiredJwtException.class})
    public ResponseEntity<ApiErrorResponse> handleExpiredJwtException(final ExpiredJwtException e) {
        final var locale = getLocale();
        final var authError = AuthError.AUTH_TOKEN_EXPIRED;
        final var message = messageSource.getMessage(authError.getMessageKey(), new Object[]{}, locale);

        final var errorResp = new ApiErrorResponse(401);
        errorResp.setErrorCode(authError.getErrorCode());
        errorResp.setMessage(message);
        errorResp.setDetail(e.getMessage());
        errorResp.setProperty("traceId", MDC.get("UUID"));
        return ResponseEntity.ok(errorResp);
    }

    private ApiErrorResponse createCommonError(final int rawStatusCode,
                                               final BaseApiException e) {
        final var locale = getLocale();
        return ApiErrorResponse.getInstance(rawStatusCode, e, messageSource, locale);
    }

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
