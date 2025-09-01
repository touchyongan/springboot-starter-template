package io.touchyongan.starter_template.feature.auth.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthClientUrlData {
    private String provider;
    private String authorizationUrl;
}
