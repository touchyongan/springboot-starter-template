package io.touchyongan.starter_template.common.specification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public abstract class BasedFilterWithPagination<T> {
    protected PaginationRequest paginationRequest;
    protected List<SearchCriteria> searchCriteriaList = new ArrayList<>();

    public Pageable getPageable() {
        return paginationRequest.getPageable();
    }

    public Specification<T> getSpecification() {
        initialSearchCriteriaList();
        return new GenericSpecification<>(searchCriteriaList);
    }

    public abstract void initialSearchCriteriaList();

    public SearchCriteria searchBetweenDate(final String dateFieldName,
                                            final LocalDate fromDate,
                                            final LocalDate toDate) {
        if (Objects.isNull(fromDate) && Objects.nonNull(toDate)) {
            final var endDate = toDate.atTime(23, 59, 59);
            return SearchCriteria.newSearchCriteria(dateFieldName, SearchOperator.LESS_THAN_OR_EQUAL, endDate);
        }
        if (Objects.isNull(toDate) && Objects.nonNull(fromDate)) {
            final var startDate = fromDate.atTime(0, 0);
            return SearchCriteria.newSearchCriteria(dateFieldName, SearchOperator.GREATER_THAN_OR_EQUAL, startDate);
        }
        if (Objects.nonNull(toDate)) {
            final var startDate = fromDate.atTime(0, 0);
            final var endDate = toDate.atTime(23, 59, 59);
            return SearchCriteria.newSearchCriteriaIn(dateFieldName, SearchOperator.BETWEEN, List.of(startDate, endDate));
        }
        return null;
    }
}
