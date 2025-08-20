package io.touchyongan.starter_template.infrastructure.permission;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * action refers to CREATE, READ, UPDATE, DELETE
 * entity refers to Entity class name
 * The format of permission code will be ENTITY:ACTION
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@PreAuthorize(value = "customCheckPermission()")
public @interface CustomPreAuthorize {
    String action() default "";
    String[] actions() default {};
    String entity();
}
