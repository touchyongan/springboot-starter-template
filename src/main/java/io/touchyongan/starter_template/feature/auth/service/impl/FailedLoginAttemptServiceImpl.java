package io.touchyongan.starter_template.feature.auth.service.impl;

import io.touchyongan.starter_template.common.specification.AggregateFunction;
import io.touchyongan.starter_template.common.util.RequestContextUtil;
import io.touchyongan.starter_template.feature.auth.entity.FailedLogin;
import io.touchyongan.starter_template.feature.auth.repository.FailedLoginRepository;
import io.touchyongan.starter_template.feature.auth.service.FailedLoginAttemptService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedLoginAttemptServiceImpl implements FailedLoginAttemptService {
    private static final int MAX_DELAY = 30; // 30 second
    private static final long ATTEMPT_TIME_WINDOW = 30; // 30 minutes

    private final FailedLoginRepository failedLoginRepository;

    @Override
    public void applyDelay(final String username) {
        final var delayTime = calculateTimeDelay(username);
        if (delayTime == 0) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(delayTime);
        } catch (final InterruptedException e) {
            log.info("Interrupt when delay failed attempt to login");
        }
    }

    private long calculateTimeDelay(final String username) {
        final var count = countFailedAttempt(username);
        if (count == 0) {
            return 0;
        }
        final var delayTime = (long) Math.pow(2, count);
        return Math.min(MAX_DELAY, delayTime);
    }

    private long countFailedAttempt(final String username) {
        final Specification<FailedLogin> spec = (root , query, builder) -> {
            final var predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("username"), username));
            predicates.add(builder.equal(root.get("isStillInAttempt"), true));

            final var attemptTimeWindow = LocalDateTime.now().minusMinutes(ATTEMPT_TIME_WINDOW);
            final var attemptDateCondition = builder.greaterThanOrEqualTo(root.get("failedAt"), attemptTimeWindow);
            predicates.add(attemptDateCondition);
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return failedLoginRepository.aggregateByField(spec, Long.class, FailedLogin.class, "username", AggregateFunction.COUNT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void loginFailed(final String username,
                            final AuthenticationException authException) {
        final var failedLogin = new FailedLogin();
        failedLogin.setFailedAt(LocalDateTime.now());
        failedLogin.setStillInAttempt(true);
        failedLogin.setErrorMsg(authException.getMessage());
        failedLogin.setUsername(username);

        final var servletAttribute = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final var request = servletAttribute.getRequest();
        final var ip = RequestContextUtil.getIPAddress(request);
        failedLogin.setIp(ip);
        failedLoginRepository.save(failedLogin);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void loginSucceed(final String username) {
        failedLoginRepository.clearFailedLoginAttempt(username);
    }
}
