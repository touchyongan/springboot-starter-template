package io.touchyongan.starter_template.common.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.Objects;

@MappedSuperclass
public class CustomPersistable implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return Objects.isNull(this.id);
    }
}
