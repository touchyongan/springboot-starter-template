package io.touchyongan.starter_template.common.specification;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class SearchCriteria {
    private final String key;
    private final Object value;
    private final List<Object> values;
    private final SearchOperator operator;
    private final List<String> keys;

    private SearchCriteria(final String key,
                           final SearchOperator operator,
                           final Object value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(operator);
        this.operator = operator;
        this.value = value;
        this.key = key;
        this.values = new ArrayList<>();
        this.keys = null;
    }

    /**
     * Provides list of value when use with operator
     * - IN
     * - NOT_IN
     * - NOT_EQUAL
     * - BETWEEN
     */
    private SearchCriteria(final String key,
                           final List<Object> values,
                           final SearchOperator operator) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(operator);
        this.operator = operator;
        this.value = null;
        this.key = key;
        this.values = values;
        this.keys = null;
    }

    private SearchCriteria(final Object value,
                           final String... fields) {
        this.operator = SearchOperator.MULTI_FIELDS_SEARCH;
        this.value = value;
        this.key = "";
        this.values = new ArrayList<>();
        this.keys = Arrays.asList(fields);
    }

    public static SearchCriteria newSearchCriteria(final String key,
                                                   final SearchOperator operator,
                                                   final Object value) {
        return new SearchCriteria(key, operator, value);
    }

    public static <T> SearchCriteria newSearchCriteriaIn(final String key,
                                                         final SearchOperator operator,
                                                         final List<T> values) {
        final var listObject = Objects.isNull(values) ? new ArrayList<>() : new ArrayList<Object>(values);
        return new SearchCriteria(key, listObject, operator);
    }

    public static SearchCriteria notNullSearch(final String key) {
        return new SearchCriteria(key, SearchOperator.NOT_NULL, "NOT_NULL");
    }

    public static SearchCriteria isNullSearch(final String key) {
        return new SearchCriteria(key, SearchOperator.IS_NULL, "IS_NULL");
    }

    public static SearchCriteria multiFieldsSearch(final String query,
                                                   final String... fields) {
        return new SearchCriteria(query, fields);
    }
}
