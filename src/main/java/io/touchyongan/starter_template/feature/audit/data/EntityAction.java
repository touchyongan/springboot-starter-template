package io.touchyongan.starter_template.feature.audit.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class EntityAction {
    private String entity;
    private Set<String> actions = new HashSet<>();

    public void addAction(final String action) {
        actions.add(action);
    }

    public List<String> getActions() {
        return actions.stream()
                .sorted()
                .toList();
    }
}
