package io.touchyongan.starter_template.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "log.security")
@Getter
public class MaskPIIInfoProperties {
    private final List<String> maskHeaders;
    private final List<String> requestBodyFields;
    private final List<Pattern> maskParamsPattern;

    public MaskPIIInfoProperties(final List<String> maskHeaders,
                                 final List<String> requestBodyFields) {
        this.maskHeaders = maskHeaders;
        this.requestBodyFields = requestBodyFields;
        this.maskParamsPattern = createParamPatterns(this.requestBodyFields);
    }

    private List<Pattern> createParamPatterns(final List<String> maskBodies) {
        final var patterns = new ArrayList<Pattern>();
        for (final var maskBody : maskBodies) {
            final var pattern = Pattern.compile("%s=([^&\\s]+)".formatted(maskBody));
            patterns.add(pattern);
        }
        return patterns;
    }
}
