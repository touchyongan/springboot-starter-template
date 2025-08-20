package io.touchyongan.starter_template.config;

import io.touchyongan.starter_template.config.properties.CrossOriginProperties;
import io.touchyongan.starter_template.config.properties.MaskPIIInfoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {
        MaskPIIInfoProperties.class,
        CrossOriginProperties.class
})
public class CustomPropertiesConfig {
}
