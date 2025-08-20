package io.touchyongan.starter_template.common.specification;

import io.touchyongan.starter_template.common.exception.custom.BaseApiException;
import io.touchyongan.starter_template.common.exception.custom.impl.InputValidationError;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
@Setter
public class SortRequest {
    private static final Set<String> VALID_DIRECTIONS = Set.of("ASC", "DESC");

    private List<String> sortFields;
    private List<String> sortDirections;

    private SortRequest() {
    }

    public static SortRequest newSortRequest(final List<String> sortFields,
                                             final List<String> sortDirections) {
        final var sortRequest = new SortRequest();
        sortRequest.sortFields = Optional.ofNullable(sortFields).orElse(List.of());
        sortRequest.sortDirections = Optional.ofNullable(sortDirections).orElse(List.of());
        sortRequest.validateSortFields();
        sortRequest.validateSortDirection();
        return sortRequest;
    }

    private void validateSortDirection() {
        if (sortDirections.isEmpty()) {
            return;
        }
        if (sortDirections.size() != sortFields.size()) {
            throw new BaseApiException(InputValidationError.INVALID_SORT_FIELD);
        }
        sortDirections.forEach(d -> {
            if (!StringUtils.hasText(d) ||
                    !VALID_DIRECTIONS.contains(d.toUpperCase())) {
                throw new BaseApiException(InputValidationError.INVALID_SORT_DIRECTION);
            }
        });
    }

    private void validateSortFields() {
        sortFields.forEach(s -> {
            if (!StringUtils.hasText(s)) {
                throw new BaseApiException(InputValidationError.INVALID_SORT_FIELD);
            }
        });
    }

    public <T> List<Order> getCriteriaOrders(final Root<T> root,
                                             final CriteriaBuilder builder) {
        final var orders = new ArrayList<Order>();
        for (var i = 0; i < sortFields.size(); i++) {
            final var direction = sortDirections.get(i);
            final var field = sortFields.get(i);
            final Order order;
            if ("desc".equalsIgnoreCase(direction)) {
                order = builder.desc(root.get(field));
            } else {
                order = builder.asc(root.get(field));
            }
            orders.add(order);
        }
        return orders;
    }

    public static void validateValidFields(final Set<String> supportedSortFields,
                                           final List<String> sortFields) {
        if (Objects.isNull(sortFields) || sortFields.isEmpty()) {
            return;
        }
        sortFields.forEach(s -> {
            if (!supportedSortFields.contains(s)) {
                throw new BaseApiException(InputValidationError.INVALID_SORT_FIELD);
            }
        });
    }
}
