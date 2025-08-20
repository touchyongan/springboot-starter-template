package io.touchyongan.starter_template.feature.user.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "roles")
@Entity
@Getter
@Setter
public class Role extends CustomPersistable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_disabled")
    private boolean disabled;

    @Column(name = "is_not_allowed_update")
    private boolean isNotAllowedUpdate;

    @Column(name = "is_system")
    private boolean system;

    @ManyToMany
    @JoinTable(name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions = new ArrayList<>();
}
