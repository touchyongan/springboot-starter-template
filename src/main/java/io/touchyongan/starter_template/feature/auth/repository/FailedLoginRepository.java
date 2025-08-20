package io.touchyongan.starter_template.feature.auth.repository;

import io.touchyongan.starter_template.common.base.BaseRepository;
import io.touchyongan.starter_template.common.specification.BaseProjectionRepository;
import io.touchyongan.starter_template.feature.auth.entity.FailedLogin;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FailedLoginRepository extends BaseRepository<FailedLogin>, BaseProjectionRepository<FailedLogin> {

    @Modifying
    @Query(value = "UPDATE failed_login SET is_still_in_attempt = false WHERE username = :username and is_still_in_attempt = true",
            nativeQuery = true)
    void clearFailedLoginAttempt(String username);
}
