package io.touchyongan.starter_template.feature.audit.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class AuditTrailFilterTemplate {
    private List<EntityAction> entityActions;
    private List<String> username;
    private List<String> status;
}
