package io.touchyongan.starter_template.feature.user.repository;

import io.touchyongan.starter_template.common.base.BaseRepository;
import io.touchyongan.starter_template.common.specification.BaseProjectionRepository;
import io.touchyongan.starter_template.feature.user.entity.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends BaseRepository<Role>, BaseProjectionRepository<Role> {
}

