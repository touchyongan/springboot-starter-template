package io.touchyongan.starter_template.feature.user.repository;

import io.touchyongan.starter_template.common.base.BaseRepository;
import io.touchyongan.starter_template.common.specification.BaseProjectionRepository;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppUserRepository extends BaseRepository<AppUser>, BaseProjectionRepository<AppUser> {

    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE LOWER(u.username) = :username OR LOWER(u.email) = :username" )
    Optional<AppUser> findByUsernameJoinFetch(String username);
}
