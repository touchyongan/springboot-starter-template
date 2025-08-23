package io.touchyongan.starter_template.common.util;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class AuditContext {
    private final Object oldValueObj;
    private final Object newValueObj;
    private final Map<String, Object> oldValueMap;
    private final Map<String, Object> newValueMap;

    public AuditContext(final Object oldValueObj,
                        final Object newValueObj) {
        this.oldValueObj = oldValueObj;
        this.newValueObj = newValueObj;
        this.oldValueMap = new LinkedHashMap<>();
        this.newValueMap = new LinkedHashMap<>();
    }

    public void addOldValue(final String key,
                            final Object value) {
        oldValueMap.put(key, value);
    }

    public void addNewValue(final String key,
                            final Object value) {
        newValueMap.put(key, value);
    }
}
