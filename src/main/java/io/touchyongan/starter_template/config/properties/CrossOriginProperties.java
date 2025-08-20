package io.touchyongan.starter_template.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security.cors")
@Getter
@RequiredArgsConstructor
public class CrossOriginProperties {
    private final List<String> allowOrigins;
    private final List<String> allowHeaders;
    private final List<String> allowMethods;
}
