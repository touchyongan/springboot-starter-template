package io.touchyongan.starter_template.feature.audit.annotation;

import io.touchyongan.starter_template.feature.audit.aop.LogActionAnonymous;
import io.touchyongan.starter_template.feature.audit.data.EntityAction;
import io.touchyongan.starter_template.infrastructure.permission.CustomPreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@Component
public class RegisterEntityAction implements SmartInitializingSingleton, ApplicationContextAware {
    private static final Map<String, EntityAction> ENTITY_ACTION_MAP = new HashMap<>();

    private ApplicationContext context;

    public void registerEntityAndAction() {
        final var start = System.currentTimeMillis();
        log.info("Start register entity and action ...");
        final var beans = context.getBeansWithAnnotation(RestController.class);
        for (final var bean : beans.values()) {
            final var clz = AopProxyUtils.ultimateTargetClass(bean);
            for (final var method : clz.getDeclaredMethods()) {
                final var customPreAuthorize = method.getAnnotation(CustomPreAuthorize.class);
                if (Objects.nonNull(customPreAuthorize)) {
                    final var entityAction = ENTITY_ACTION_MAP.computeIfAbsent(customPreAuthorize.entity(), key -> new EntityAction());
                    entityAction.setEntity(customPreAuthorize.entity());
                    entityAction.addAction(customPreAuthorize.action());
                }

                final var logActionAnonymous = method.getAnnotation(LogActionAnonymous.class);
                if (Objects.nonNull(logActionAnonymous)) {
                    final var entityAction = ENTITY_ACTION_MAP.computeIfAbsent(logActionAnonymous.entity(), key -> new EntityAction());
                    entityAction.setEntity(logActionAnonymous.entity());
                    entityAction.addAction(logActionAnonymous.action());
                }
            }
        }
        final var finish = (System.currentTimeMillis() - start) / 1000.0;
        log.info("Finish register entity and action with {} seconds", finish);
    }

    public List<EntityAction> getEntityAction() {
        return ENTITY_ACTION_MAP.values()
                .stream()
                .sorted(Comparator.comparing(EntityAction::getEntity))
                .toList();
    }

    @Override
    public void afterSingletonsInstantiated() {
        registerEntityAndAction();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
