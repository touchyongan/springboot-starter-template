package io.touchyongan.starter_template.infrastructure.permission;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.*;
import java.util.stream.Collectors;

public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private final MethodInvocation invocation;
    private Object filterObject;
    private Object returnObject;

    public CustomSecurityExpressionRoot(final Authentication authentication,
                                        final MethodInvocation invocation) {
        super(authentication);
        this.invocation = invocation;
    }

    public boolean customCheckPermission() {
        final var authentication = this.getAuthentication();
        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            return false;
        }

        final var requiredPermissions = new HashSet<String>();
        requiredPermissions.add(getPermissionCode());
        requiredPermissions.addAll(getPermissionCodes());
        for (final var authority : authentication.getAuthorities()) {
            for (final var required : requiredPermissions) {
                if (hasAnyPermission(authority.getAuthority(), required)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAnyPermission(final String authority,
                                     final String permission) {
        return hasAllFunctionsPermission(authority) ||
                hasAllReadFunctionsPermission(authority) ||
                hasAllWriteFunctionsPermission(authority) ||
                hasPermissionFor(authority, permission);
    }

    private boolean hasPermissionFor(final String authority,
                                     final String permission) {
        return authority.equalsIgnoreCase(permission);
    }

    private boolean hasAllFunctionsPermission(final String authority) {
        return "SPECIAL:ALL_FUNCTIONS".equalsIgnoreCase(authority);
    }

    private boolean hasAllReadFunctionsPermission(final String authority) {
        return "SPECIAL:ALL_READ_FUNCTIONS".equalsIgnoreCase(authority);
    }

    private boolean hasAllWriteFunctionsPermission(final String authority) {
        return "SPECIAL:ALL_WRITE_FUNCTIONS".equalsIgnoreCase(authority);
    }

    private String getPermissionCode() {
        final var method = invocation.getMethod();
        final var customPreAuthorize = method.getAnnotation(CustomPreAuthorize.class);

        return Optional.ofNullable(customPreAuthorize)
                .map(ca -> "%s:%s".formatted(customPreAuthorize.entity().toUpperCase(), customPreAuthorize.action().toUpperCase()))
                .orElse("");
    }

    private Set<String> getPermissionCodes() {
        final var method = invocation.getMethod();
        final var customPreAuthorize = method.getAnnotation(CustomPreAuthorize.class);

        return Optional.ofNullable(customPreAuthorize)
                .map(as -> Arrays.asList(as.actions()))
                .stream()
                .flatMap(Collection::stream)
                .map(ca -> "%s:%s".formatted(customPreAuthorize.entity().toUpperCase(), customPreAuthorize.action().toUpperCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public void setFilterObject(final Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(final Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
