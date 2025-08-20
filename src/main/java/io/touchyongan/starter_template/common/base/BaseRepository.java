package io.touchyongan.starter_template.common.base;

import io.touchyongan.starter_template.common.exception.custom.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    default T findByIdThrowExceptionIfNotFound(final Long id, final Class<T> cls) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException(cls.getSimpleName(), id));
    }
}
