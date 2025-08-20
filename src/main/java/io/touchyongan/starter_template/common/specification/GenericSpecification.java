package io.touchyongan.starter_template.common.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericSpecification<T> implements Specification<T> {
    @Serial
    private final static long serialVersionUID = 1;

    private transient List<SearchCriteria> listCriteria;

    public GenericSpecification(final List<SearchCriteria> listCriteria) {
        this.listCriteria = listCriteria;
    }

    @Override
    public Predicate toPredicate(final Root<T> root,
                                 final CriteriaQuery<?> query,
                                 final CriteriaBuilder builder) {
        final var predicates = new ArrayList<Predicate>();
        for (final var criteria : listCriteria) {
            final var operator = criteria.getOperator();
            if (criteria.getOperator() == SearchOperator.MULTI_FIELDS_SEARCH) {
                final var orPredicates = new ArrayList<Predicate>();
                for (final var key : criteria.getKeys()) {
                    final String field;
                    final Path<?> path;
                    if (key.contains(".")) {
                        final var sp = key.split("\\.");
                        field = sp[0];
                        var join = root.join(field);
                        for (var i = 1; i < sp.length - 1; i++) {
                            join = join.join(sp[i]);
                        }
                        path = join.get(sp[sp.length - 1]);
                    } else {
                        path = root.get(key);
                    }
                    final var searchTerm = "%" + criteria.getValue().toString().toLowerCase() + "%";
                    orPredicates.add(builder.like(builder.lower(path.as(String.class)), searchTerm));
                }
                if (!orPredicates.isEmpty()) {
                    predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
                }
            } else if (Objects.nonNull(criteria.getValue()) &&
                    StringUtils.hasText(criteria.getValue().toString())) {
                final String field;
                final Path<?> path;
                if (criteria.getKey().contains(".")) {
                    final var sp = criteria.getKey().split("\\.");
                    field = sp[0];
                    var join = root.join(field);
                    for (var i = 1; i < sp.length - 1; i++) {
                        join = join.join(sp[i]);
                    }
                    path = join.get(sp[sp.length - 1]);
                } else {
                    path = root.get(criteria.getKey());
                }
                final var attributeType = path.getJavaType();

                final var searchTerm = "%" + criteria.getValue().toString().toLowerCase() + "%";

                switch (operator) {
                    case EQUAL -> predicates.add(builder.equal(path, criteria.getValue()));
                    case STR_EQUAL_IGNORE_CASE -> predicates.add(builder.equal(builder.lower(path.as(String.class)),
                            criteria.getValue().toString().toLowerCase()));
                    case NOT_EQUAL -> predicates.add(builder.notEqual(path, criteria.getValue()));
                    case GREATER_THAN -> addGreaterThanPredicate(builder, path, attributeType, criteria, predicates);
                    case GREATER_THAN_OR_EQUAL -> addGreaterOrEqualsPredicate(builder, path, attributeType, criteria, predicates);
                    case LESS_THAN -> addLessThanPredicate(builder, path, attributeType, criteria, predicates);
                    case LESS_THAN_OR_EQUAL -> addLessThanOrEqualsPredicate(builder, path, attributeType, criteria, predicates);
                    case LIKE -> {
                        predicates.add(builder.like(builder.lower(path.as(String.class)), searchTerm));
                    }
                    case NOT_NULL -> predicates.add(builder.isNotNull(path));
                    case IS_NULL -> predicates.add(builder.isNull(path));
                    default -> {
                    }
                }
            } else if (!criteria.getValues().isEmpty()) {
                final String field;
                final Path<?> path;
                if (criteria.getKey().contains(".")) {
                    final var sp = criteria.getKey().split("\\.");
                    field = sp[0];
                    var join = root.join(field);
                    for (var i = 1; i < sp.length - 1; i++) {
                        join = join.join(sp[i]);
                    }
                    path = join.get(sp[sp.length - 1]);
                } else {
                    path = root.get(criteria.getKey());
                }
                final var attributeType = path.getJavaType();
                switch (operator) {
                    case BETWEEN -> addBetweenPredicate(builder, path, attributeType, criteria, predicates);
                    case IN -> predicates.add(path.in(criteria.getValues()));
                    case NOT_IN -> predicates.add(builder.not(path.in(criteria.getValues())));
                    default -> {
                    }
                }
            }
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    @Serial
    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.listCriteria = new ArrayList<>();
    }

    private void addGreaterThanPredicate(final CriteriaBuilder builder,
                                         final Path<?> path,
                                         final Class<?> attributeType,
                                         final SearchCriteria criteria,
                                         final List<Predicate> predicates) {
        if (Integer.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(Integer.class), (Integer) criteria.getValue()));
        } else if (Double.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(Double.class), (Double) criteria.getValue()));
        } else if (Float.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(Float.class), (Float) criteria.getValue()));
        } else if (BigDecimal.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(BigDecimal.class), (BigDecimal) criteria.getValue()));
        } else if (LocalDateTime.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(LocalDateTime.class), (LocalDateTime) criteria.getValue()));
        } else if (LocalDate.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(LocalDate.class), (LocalDate) criteria.getValue()));
        } else if (String.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(String.class), (String) criteria.getValue()));
        } else {
            throw new IllegalArgumentException("Unsupported attribute type for greaterThan comparison: " + attributeType);
        }
    }

    private void addGreaterOrEqualsPredicate(final CriteriaBuilder builder,
                                             final Path<?> path,
                                             final Class<?> attributeType,
                                             final SearchCriteria criteria,
                                             final List<Predicate> predicates) {
        if (Integer.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(Integer.class), (Integer) criteria.getValue()));
        } else if (Double.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(Double.class), (Double) criteria.getValue()));
        } else if (Float.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(Float.class), (Float) criteria.getValue()));
        } else if (BigDecimal.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(BigDecimal.class), (BigDecimal) criteria.getValue()));
        } else if (LocalDateTime.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(LocalDateTime.class), (LocalDateTime) criteria.getValue()));
        } else if (LocalDate.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThan(path.as(LocalDate.class), (LocalDate) criteria.getValue()));
        } else if (String.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.greaterThanOrEqualTo(path.as(String.class), (String) criteria.getValue()));
        } else {
            throw new IllegalArgumentException("Unsupported attribute type for greaterThanOrEqualTo comparison: " + attributeType);
        }
    }

    private void addLessThanPredicate(final CriteriaBuilder builder,
                                      final Path<?> path,
                                      final Class<?> attributeType,
                                      final SearchCriteria criteria,
                                      final List<Predicate> predicates) {
        if (Integer.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(Integer.class), (Integer) criteria.getValue()));
        } else if (Double.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(Double.class), (Double) criteria.getValue()));
        } else if (Float.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(Float.class), (Float) criteria.getValue()));
        } else if (BigDecimal.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(BigDecimal.class), (BigDecimal) criteria.getValue()));
        } else if (LocalDateTime.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(LocalDateTime.class), (LocalDateTime) criteria.getValue()));
        } else if (LocalDate.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(LocalDate.class), (LocalDate) criteria.getValue()));
        } else if (String.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThan(path.as(String.class), (String) criteria.getValue()));
        } else {
            throw new IllegalArgumentException("Unsupported attribute type for lessThan comparison: " + attributeType);
        }
    }

    private void addLessThanOrEqualsPredicate(final CriteriaBuilder builder,
                                              final Path<?> path,
                                              final Class<?> attributeType,
                                              final SearchCriteria criteria,
                                              final List<Predicate> predicates) {
        if (Integer.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(Integer.class), (Integer) criteria.getValue()));
        } else if (Double.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(Double.class), (Double) criteria.getValue()));
        } else if (Float.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(Float.class), (Float) criteria.getValue()));
        } else if (BigDecimal.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(BigDecimal.class), (BigDecimal) criteria.getValue()));
        } else if (LocalDateTime.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(LocalDateTime.class), (LocalDateTime) criteria.getValue()));
        } else if (LocalDate.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(LocalDate.class), (LocalDate) criteria.getValue()));
        } else if (String.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.lessThanOrEqualTo(path.as(String.class), (String) criteria.getValue()));
        } else {
            throw new IllegalArgumentException("Unsupported attribute type for lessThanOrEqualTo comparison: " + attributeType);
        }
    }

    private void addBetweenPredicate(final CriteriaBuilder builder,
                                     final Path<?> path,
                                     final Class<?> attributeType,
                                     final SearchCriteria criteria,
                                     final List<Predicate> predicates) {
        if (Integer.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(Integer.class), (Integer) criteria.getValues().get(0),
                    (Integer) criteria.getValues().get(1)));
        } else if (Double.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(Double.class), (Double) criteria.getValues().get(0),
                    (Double) criteria.getValues().get(1)));
        } else if (Float.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(Float.class), (Float) criteria.getValues().get(0),
                    (Float) criteria.getValues().get(1)));
        } else if (BigDecimal.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(BigDecimal.class), (BigDecimal) criteria.getValues().get(0),
                    (BigDecimal) criteria.getValues().get(1)));
        } else if (LocalDateTime.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(LocalDateTime.class), (LocalDateTime) criteria.getValues().get(0),
                    (LocalDateTime) criteria.getValues().get(1)));
        } else if (LocalDate.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(LocalDate.class), (LocalDate) criteria.getValues().get(0),
                    (LocalDate) criteria.getValues().get(1)));
        } else if (String.class.isAssignableFrom(attributeType)) {
            predicates.add(builder.between(path.as(String.class), (String) criteria.getValues().get(0),
                    (String) criteria.getValues().get(1)));
        } else {
            throw new IllegalArgumentException("Unsupported attribute type for between comparison: " + attributeType);
        }
    }
}
