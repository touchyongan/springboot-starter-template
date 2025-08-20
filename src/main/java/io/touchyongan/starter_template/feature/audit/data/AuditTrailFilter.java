package io.touchyongan.starter_template.feature.audit.data;

import io.touchyongan.starter_template.common.exception.custom.BaseApiException;
import io.touchyongan.starter_template.common.exception.custom.impl.GeneralError;
import io.touchyongan.starter_template.common.specification.BasedFilterWithPagination;
import io.touchyongan.starter_template.common.specification.SearchCriteria;
import io.touchyongan.starter_template.common.specification.SearchOperator;
import io.touchyongan.starter_template.feature.audit.entity.ActionStatus;
import io.touchyongan.starter_template.feature.audit.entity.AuditTrail;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class AuditTrailFilter extends BasedFilterWithPagination<AuditTrail> {
    public static final Set<String> SUPPORTED_FIELDS = Set.of("id", "action", "entity", "createdAt", "username");

    private List<Long> resourceIds;
    private String action;
    private String entity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String username;
    private String status;

    public static void validateStatus(final String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        if (ActionStatus.from(status) == ActionStatus.UNKNOWN) {
            final var validStatus = Arrays.stream(ActionStatus.values())
                    .filter(s -> s != ActionStatus.UNKNOWN)
                    .map(ActionStatus::getStatus)
                    .toList();
            throw new BaseApiException(GeneralError.INVALID_ENUM, status, "[%s]".formatted(String.join(",", validStatus)));
        }
    }

    @Override
    public void initialSearchCriteriaList() {
        searchCriteriaList.add(filterResourceIds());
        searchCriteriaList.add(filterAction());
        searchCriteriaList.add(filterEntity());
        searchCriteriaList.add(filterUsername());
        if (StringUtils.hasText(status)) {
            searchCriteriaList.add(filterStatus());
        }
        searchCriteriaList.add(filterStartDate());
        searchCriteriaList.add(filterEndDate());

        // Add defaults filter to exclude audit log of view audit trail
        searchCriteriaList.add(SearchCriteria.newSearchCriteria("entity", SearchOperator.NOT_EQUAL, AuditTrailConstant.ENTITY));
    }

    private SearchCriteria filterResourceIds() {
        return SearchCriteria.newSearchCriteriaIn("resourceId", SearchOperator.IN, resourceIds);
    }

    private SearchCriteria filterAction() {
        return SearchCriteria.newSearchCriteria("action", SearchOperator.EQUAL, action);
    }

    private SearchCriteria filterEntity() {
        return SearchCriteria.newSearchCriteria("entity", SearchOperator.EQUAL, entity);
    }

    private SearchCriteria filterUsername() {
        return SearchCriteria.newSearchCriteria("username", SearchOperator.EQUAL, username);
    }

    private SearchCriteria filterStatus() {
        final var statusEnum = ActionStatus.from(status);
        return SearchCriteria.newSearchCriteria("status", SearchOperator.EQUAL, statusEnum);
    }

    private SearchCriteria filterStartDate() {
        return SearchCriteria.newSearchCriteria("createdAt", SearchOperator.GREATER_THAN_OR_EQUAL, startDate);
    }

    private SearchCriteria filterEndDate() {
        return SearchCriteria.newSearchCriteria("createdAt", SearchOperator.LESS_THAN_OR_EQUAL, endDate);
    }
}
