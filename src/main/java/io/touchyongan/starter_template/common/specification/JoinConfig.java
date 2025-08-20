package io.touchyongan.starter_template.common.specification;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;

import java.util.List;

@Getter
public class JoinConfig {
    private final String joinPropertyName;
    private final List<JoinType> joinType;
    private final String entityPropertyName;
    private final String dtoPropertyName;

    public JoinConfig(final String joinPropertyName,
                      final List<JoinType> joinType,
                      final String entityPropertyName,
                      final String dtoPropertyName) {
        this.joinPropertyName = joinPropertyName;
        this.joinType = joinType;
        this.entityPropertyName = entityPropertyName;
        this.dtoPropertyName = dtoPropertyName;
    }
}
