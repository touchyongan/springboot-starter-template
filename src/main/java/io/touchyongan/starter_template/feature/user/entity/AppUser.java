package io.touchyongan.starter_template.feature.user.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Table(name = "app_users")
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE app_users SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction(value = "is_deleted = false")
public class AppUser extends CustomPersistable implements UserDetails {

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "is_account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "is_account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "is_credentials_non_expired")
    private boolean credentialsNonExpired;

    @Column(name = "is_enabled")
    private boolean enabled;

    @Column(name = "last_time_password_updated")
    private LocalDateTime lastTimePasswordUpdated;

    @Column(name = "is_not_allowed_update")
    private boolean isNotAllowedUpdate;

    @Column(name = "is_system")
    private boolean system;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final var authorities = roles.stream()
                .map(Role::getName)
                .map(n -> new SimpleGrantedAuthority("ROLE_" + n))
                .collect(Collectors.toList());
        final var permissions = roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .map(SimpleGrantedAuthority::new)
                .toList();
        authorities.addAll(permissions);
        return authorities;
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
