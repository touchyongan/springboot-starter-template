package io.touchyongan.starter_template.common.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * This class design to reuse for implement audit change of any entity class.
 * To keep it simple, this class design with convention of entity and dto class must have the same field name.
 * Currently, this class design to work with compare simple type. If you have any complex case, and it is rare,
 * then write manual compare and track it. If you find it is a repeated use-case, then you can try to extend or create a new one
 */
public final class AuditUtil {

    private AuditUtil() {
    }

    public static void auditUpdateAndSetLogChangeContext(final Object entity,
                                                         final Object dto) {
        final var auditContext = auditAndUpdate(entity, dto);
        setLogChangeContext(auditContext.getOldValueMap(), auditContext.getNewValueMap());
    }

    public static AuditContext auditAndUpdate(final Object entity,
                                              final Object dto) {
        final var auditContext = new AuditContext(entity, dto);
        auditAndUpdateRecursive(entity, dto, auditContext.getOldValueMap(), auditContext.getNewValueMap(), "");
        return auditContext;
    }

    public static void setLogChangeContext(final Map<String, Object> oldValue,
                                           final Map<String, Object> newValue) {
        RequestContextUtil.put(RequestContextUtil.KEY_OLD_VALUE, oldValue);
        RequestContextUtil.put(RequestContextUtil.KEY_NEW_VALUE, newValue);
    }

    private static void auditAndUpdateRecursive(final Object entity,
                                                final Object dto,
                                                final Map<String, Object> oldValueMap,
                                                final Map<String, Object> newValueMap,
                                                final String pathPrefix) {
        final var entityClz = entity.getClass();
        final var dtoClz = dto.getClass();

        for (final var dtoField : dtoClz.getDeclaredFields()) {
            final var auditChange = dtoField.getDeclaredAnnotation(AuditChange.class);
            final var newValue = getFieldValue(dtoField, dto);
            if (isIgnoreField(auditChange) || isIgnoreUpdateNullValue(auditChange, newValue)) {
                continue;
            }

            final var entityFieldName = getEntityFieldNameFromDto(dtoField, auditChange);
            final var entityField = ReflectionUtils.findField(entityClz, entityFieldName);
            if (Objects.isNull(entityField)) {
                continue;
            }
            final var oldValue = getFieldValue(entityField, entity);


            if (isSimpleType(dtoField.getType())) {
                final var pathField = pathPrefix + dtoField.getName();
                if (AppUtil.hasChanged(oldValue, newValue)) {
                    oldValueMap.put(pathField, oldValue);
                    newValueMap.put(pathField, newValue);

                    setFieldValue(entityField, entity, newValue);
                }
            } else {
                final var newPathPrefix = pathPrefix + "%s.".formatted(entityFieldName);
                auditAndUpdateRecursive(oldValue, newValue, oldValueMap, newValueMap, newPathPrefix);
            }
        }
    }

    private static boolean isIgnoreUpdateNullValue(final AuditChange auditChange,
                                                   final Object newValue) {
        // If audit change is null, it means no config
        if (Objects.isNull(auditChange) || !auditChange.ignoreUpdateNullValue()) {
            return false;
        }
        if (newValue instanceof String newStr) {
            return AppUtil.isBlank(newStr);
        } else {
            return Objects.isNull(newValue);
        }
    }

    private static boolean isIgnoreField(final AuditChange auditChange) {
        // If audit change is null, it means no config
        return !Objects.isNull(auditChange) && auditChange.ignoreField();
    }

    private static Object getFieldValue(final Field field,
                                        final Object object) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, object);
    }

    private static void setFieldValue(final Field field,
                                      final Object object,
                                      final Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, object, value);
    }

    private static String getEntityFieldNameFromDto(final Field dtoField,
                                                    final AuditChange auditChange) {
        if (Objects.isNull(auditChange)) {
            return dtoField.getName();
        }
        final var fieldName = auditChange.entityField();
        return AppUtil.isNotBlank(fieldName) ? fieldName : dtoField.getName();
    }

    private static boolean isSimpleType(final Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                Boolean.class.equals(type) ||
                type.isEnum() ||
                Temporal.class.isAssignableFrom(type) ||
                Collection.class.isAssignableFrom(type);
    }
}

