package io.touchyongan.starter_template.feature.user.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "user_info")
@Entity
@Getter
@Setter
public class UserInfo extends CustomPersistable {
    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "dob")
    private LocalDateTime dataOfBirth;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private AppUser appUser;
}
