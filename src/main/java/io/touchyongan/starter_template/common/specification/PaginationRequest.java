package io.touchyongan.starter_template.common.specification;

import io.touchyongan.starter_template.common.exception.custom.BaseApiException;
import io.touchyongan.starter_template.common.exception.custom.impl.InputValidationError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
@Setter
public class PaginationRequest {
    private static final Set<String> VALID_DIRECTIONS = Set.of("ASC", "DESC");
    private int page;
    private int size;
    private List<String> sortFields;
    private List<String> sortDirections;

    private PaginationRequest(final int page,
                              final int size,
                              final List<String> sortFields,
                              final List<String> sortDirections) {
        this.page = page;
        this.size = size;
        this.sortFields = Optional.ofNullable(sortFields)
                .orElse(new ArrayList<>());
        this.sortDirections = Optional.ofNullable(sortDirections)
                .orElse(new ArrayList<>());

    }

    public static PaginationRequest newPaginationRequest(final int page,
                                                         final int size,
                                                         final List<String> sortFields,
                                                         final List<String> sortDirections) {
        final var pageReq = new PaginationRequest(page, size, sortFields, sortDirections);
        pageReq.validateSortFields();
        pageReq.validateSortDirection();
        return pageReq;
    }

    public Pageable getPageable() {
        return PageRequest.of(page, size, getSort());
    }

    private Sort getSort() {
        if (sortFields.isEmpty()) {
            return Sort.unsorted();
        }
        final var orders = new ArrayList<Sort.Order>();
        for (var i = 0; i < sortFields.size(); i++) {
            final var direction = i >= sortDirections.size() ? Sort.Direction.ASC :
                    "ASC".equalsIgnoreCase(sortDirections.get(i)) ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, sortFields.get(i)));
        }
        return Sort.by(orders);
    }

    private void validateSortDirection() {
        if (sortDirections.isEmpty()) {
            return;
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
