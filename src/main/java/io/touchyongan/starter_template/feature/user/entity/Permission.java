package io.touchyongan.starter_template.feature.user.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Table(name = "permissions")
@Entity
@Setter
@Getter
public class Permission extends CustomPersistable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "code")
    private String code;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "description")
    private String description;

    @Column(name = "group")
    private String group;
}
