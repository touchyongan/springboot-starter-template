package io.touchyongan.starter_template.feature.user.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    private String dataOfBirth;
}
