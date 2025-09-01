package io.touchyongan.starter_template.feature.user.repository;

import io.touchyongan.starter_template.common.base.BaseRepository;
import io.touchyongan.starter_template.feature.user.entity.UserInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends BaseRepository<UserInfo> {
}
