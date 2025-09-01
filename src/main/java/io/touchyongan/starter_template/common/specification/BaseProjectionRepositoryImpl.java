package io.touchyongan.starter_template.common.specification;

import io.touchyongan.starter_template.common.data.CustomPage;
import io.touchyongan.starter_template.common.exception.custom.ResourceNotFoundException;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Repository
@Slf4j
public class BaseProjectionRepositoryImpl<ET> implements BaseProjectionRepository<ET> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <DT> DT findByIdWithSingleFieldProjection(final Long id,
                                                     final Class<DT> clsResult,
                                                     final Class<ET> clsEntity,
                                                     final String fieldName) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);
        query.select(root.get(fieldName));
        query.where(builder.equal(root.get("id"), id));
        final var typedQuery = entityManager.createQuery(query);
        return getSingleResult(typedQuery, id, clsEntity);
    }

    @Override
    public <DT> List<DT> findByIdWithListOfSingleFieldProjection(final Long id,
                                                                 final Class<DT> clsResult,
                                                                 final Class<ET> clsEntity,
                                                                 final String fieldName) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);
        query.select(root.get(fieldName));
        query.where(builder.equal(root.get("id"), id));
        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public <DT> List<DT> findByIdWithListOfSingleFieldProjection(final Long id,
                                                                 final Class<DT> clsResult,
                                                                 final Class<ET> clsEntity,
                                                                 final String fieldName,
                                                                 final Sort.Direction direction) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);
        query.select(root.get(fieldName));
        query.where(builder.equal(root.get("id"), id));
        final Order order;
        if (direction == Sort.Direction.ASC) {
            order = builder.asc(root.get(fieldName));
        } else {
            order = builder.desc(root.get(fieldName));
        }
        query.orderBy(order);
        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public <DT> List<DT> findBySpecificationWithListOfSingleFieldProjection(final Specification<ET> specification,
                                                                            final Class<DT> clsResult,
                                                                            final Class<ET> clsEntity,
                                                                            final String fieldName) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);
        query.select(root.get(fieldName));
        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);
        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public <DT> List<DT> findBySpecificationWithListOfSingleFieldProjection(final Specification<ET> specification,
                                                                            final Class<DT> clsResult,
                                                                            final Class<ET> clsEntity,
                                                                            final String fieldName,
                                                                            final Sort.Direction direction) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);
        query.select(root.get(fieldName));
        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);
        final Order order;
        if (direction == Sort.Direction.ASC) {
            order = builder.asc(root.get(fieldName));
        } else {
            order = builder.desc(root.get(fieldName));
        }
        query.orderBy(order);
        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DT extends Number> DT aggregateByField(final Specification<ET> specification,
                                                   final Class<DT> clsResult,
                                                   final Class<ET> clsEntity,
                                                   final String fieldName,
                                                   final AggregateFunction aggregateFunction) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsResult);
        final var root = query.from(clsEntity);

        // Applying aggregate function
        switch (aggregateFunction) {
            case SUM -> query.select(builder.sum(root.get(fieldName)));
            case AVG -> query.select(builder.avg(root.get(fieldName)).as(clsResult));
            case MAX -> query.select(builder.max(root.get(fieldName)));
            case MIN -> query.select(builder.min(root.get(fieldName)));
            case COUNT -> query.select((Selection<? extends DT>) builder.count(root.get(fieldName)));
            case COUNT_DISTINCT -> query.select((Selection<? extends DT>) builder.countDistinct(root.get(fieldName)));
        }

        // Adding where clause
        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);

        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    @Override
    public <DT> DT findByIdWithProjection(final Long id,
                                          final Class<DT> clsDTO,
                                          final Class<ET> clsEntity) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsDTO);
        final var root = query.from(clsEntity);
        final var selectPaths = getSelectPaths(root, clsDTO);
        query.select(builder.construct(clsDTO, selectPaths.toArray(new Selection[0])));
        query.where(builder.equal(root.get("id"), id));
        final var typedQuery = entityManager.createQuery(query);
        return getSingleResult(typedQuery, id, clsEntity);
    }

    private <DT> DT getSingleResult(final TypedQuery<DT> typedQuery,
                                    final Long id,
                                    final Class<ET> clsEntity) {
        try {
            return typedQuery.getSingleResult();
        } catch (final NoResultException e) {
            throw new ResourceNotFoundException(clsEntity.getSimpleName(), id);
        }
    }

    @Override
    public <DT> List<DT> findAllByIdWithProjection(final List<Long> ids,
                                                   final Class<DT> clsDTO,
                                                   final Class<ET> clsEntity) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsDTO);
        final var root = query.from(clsEntity);
        final var selectPaths = getSelectPaths(root, clsDTO);
        query.select(builder.construct(clsDTO, selectPaths.toArray(new Selection[0])));
        query.where(root.get("id").in(ids));
        final var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public <DT> List<DT> findAllWithSpecification(final Specification<ET> specification,
                                                  final Class<DT> clsDTO,
                                                  final Class<ET> clsEntity) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsDTO);
        final var root = query.from(clsEntity);
        final var selectPaths = getSelectPaths(root, clsDTO);
        query.select(builder.construct(clsDTO, selectPaths.toArray(new Selection[0])));

        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);

        final var typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList();
    }

    @Override
    public <DT> List<DT> findAllWithSpecification(final Specification<ET> specification,
                                                  final SortRequest sort,
                                                  final Class<DT> clsDTO,
                                                  final Class<ET> clsEntity) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsDTO);
        final var root = query.from(clsEntity);
        final var selectPaths = getSelectPaths(root, clsDTO);
        query.select(builder.construct(clsDTO, selectPaths.toArray(new Selection[0])));

        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);
        if (Objects.nonNull(sort)) {
            query.orderBy(sort.getCriteriaOrders(root, builder));
        }
        final var typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList();
    }

    @Override
    public <DT> CustomPage<DT> findAllWithSpecification(final Specification<ET> specification,
                                                        final Pageable pageable,
                                                        final Class<DT> clsDTO,
                                                        final Class<ET> clsEntity) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(clsDTO);
        final var root = query.from(clsEntity);
        final var selectPaths = getSelectPaths(root, clsDTO);
        query.select(builder.construct(clsDTO, selectPaths.toArray(new Selection[0])));

        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());
        final var predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);

        // Set default sore as id of root entity if not provide
        if (Objects.equals(pageable.getSort(), Sort.unsorted())) {
            query.orderBy(builder.asc(root.get("id")));
        } else {
            query.orderBy(getOrderFromPageable(root, builder, pageable));
        }

        final var typedQuery = entityManager.createQuery(query);

        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Get total count
        final var countQuery = builder.createQuery(Long.class);
        final var countRoot = countQuery.from(clsEntity);
        final var countPredicate = spec.toPredicate(countRoot, countQuery, builder);
        countQuery.select(builder.count(countRoot));
        countQuery.where(countPredicate);

        final var total = entityManager.createQuery(countQuery).getSingleResult();
        final var content = typedQuery.getResultList();

        return new CustomPage<>(content, pageable, total);
    }

    private <DT> List<Selection<Object>> getSelectPaths(final Root<ET> root,
                                                        final Class<DT> clsDTO) {
        final var fieldNames = getSelectFieldName(clsDTO);
        final var selectPaths = new ArrayList<Selection<Object>>();
        for (final var fieldName : fieldNames) {
            selectPaths.add(root.get(fieldName).alias(fieldName));
        }
        return selectPaths;
    }

    private <DT> List<String> getSelectFieldName(final Class<DT> clsDTO) {
        final var fields = getSelectField(clsDTO);
        final var fieldNames = new ArrayList<String>();
        for (final var field : fields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    private <DT> List<Field> getSelectField(final Class<DT> clsDTO) {
        final var fields = clsDTO.getDeclaredFields();
        final var fieldNames = new ArrayList<Field>();
        for (final var field : fields) {
            final var ignore = field.getDeclaredAnnotation(IgnoreFieldSelection.class);
            if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers()) || Objects.nonNull(ignore)) {
                continue;
            }
            fieldNames.add(field);
        }
        return fieldNames;
    }


    // This part for using projection and join one-to-many and one-to-one
    @Override
    public <DT> CustomPage<DT> findAllWithSpecificationWithRelationshipProjection(final Specification<ET> specification,
                                                                                  final Pageable pageable,
                                                                                  final List<JoinConfig> joinConfigs,
                                                                                  final Class<ET> clsEntity,
                                                                                  final Class<DT> clsDTO) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createTupleQuery();
        final var root = query.from(clsEntity);

        // Dynamically build joins based on the provided list of join tables
        final var joins = new ArrayList<Join<?, ?>>();
        final var aliasType = joinConfigs.stream().
                reduce(new HashMap<String, String>(), (m, c) -> {
                    m.put(c.getEntityPropertyName(), c.getDtoPropertyName());
                    return m;
                }, (m, c) -> {
                    m.putAll(c);
                    return m;
                });

        for (final var joinTable : joinConfigs) {
            // Example of mapping many-to-many, Student contains courseStudents and courseStudent contain student and course
            // So if root from student table it will join student with student_course and student_course with course
            final var joinTableParts = joinTable.getJoinPropertyName().split("\\.");
            Join<?, ?> join = root.join(joinTableParts[0], joinTable.getJoinType().getFirst());
            for (int i = 1; i < joinTableParts.length; i++) {
                join = join.join(joinTableParts[i], joinTable.getJoinType().get(i));
            }
            joins.add(join);
        }

        // Build the select clause for root entity
        final var selections = new ArrayList<Selection<?>>();
        //final var fields = clsDTO.getDeclaredFields();
        final var fields = getSelectField(clsDTO);
        final var fieldJoinDTONames = joinConfigs.stream()
                .map(JoinConfig::getDtoPropertyName)
                .toList();
        for (final var field : fields) {
            if (!fieldJoinDTONames.contains(field.getName())) {
                selections.add(root.get(field.getName()).alias(field.getName()));
            }
        }

        // Build select clause for join table of dto class
        for (final var join : joins) {
            //final var fieldJoinEntityName = join.getModel().getBindableJavaType().getSimpleName().toLowerCase();
            final var fieldJoinEntityName = join.getAttribute().getName();
            final var joinFieldName = aliasType.get(fieldJoinEntityName);
            final var joinField = fields.stream()
                    .filter(f -> f.getName().equals(joinFieldName))
                    .findFirst().get();

            final List<Field> subFields;
            if (Collection.class.isAssignableFrom(joinField.getType())) {
                final var parameterType = (ParameterizedType) joinField.getGenericType();
                final var typeOfJoinField = (Class<?>) parameterType.getActualTypeArguments()[0];
                //subFields = typeOfJoinField.getDeclaredFields();
                subFields = getSelectField(typeOfJoinField);
            } else {
                //subFields = joinField.getType().getDeclaredFields();
                subFields = getSelectField(joinField.getType());
            }
            for (final var subField : subFields) {
                // Use alias format joinFieldName.fieldName for mapping result
                // Example Student contains many course so it will courses.id , courses.name etc
                selections.add(join.get(subField.getName()).alias(joinFieldName + "." + subField.getName()));
            }
        }

        query.multiselect(selections.toArray(new Selection<?>[0]));

        final var spec = Optional.ofNullable(specification).orElse(getDefualtSpecification());

        // Create a subquery to fetch unique IDs of the root entity
        // Fixed error selects distinct sort
        final var idQuery = builder.createQuery(Tuple.class);
        final var subRoot = idQuery.from(clsEntity);
        final var idCols = new ArrayList<Selection<?>>();
        idCols.add(subRoot.get("id"));
        for (final var sort : pageable.getSort()) {
            if (!Objects.equals("id", sort.getProperty())) {
                idCols.add(subRoot.get(sort.getProperty()));
            }
        }
        idQuery.multiselect(idCols.toArray(new Selection[0]));
        idQuery.distinct(true);
        final var subPredicate = spec.toPredicate(subRoot, idQuery, builder);
        idQuery.where(subPredicate);

        if (Objects.equals(pageable.getSort(), Sort.unsorted())) {
            idQuery.orderBy(builder.asc(subRoot.get("id")));
        } else {
            idQuery.orderBy(getOrderFromPageable(subRoot, builder, pageable));
        }

        final var subQueryTyped = entityManager.createQuery(idQuery);
        subQueryTyped.setFirstResult((int) pageable.getOffset());
        subQueryTyped.setMaxResults(pageable.getPageSize());

        final var ids = subQueryTyped.getResultList();
        if (ids.isEmpty()) {
            return new CustomPage<>(Collections.emptyList(), pageable, 0);
        }

        query.where(root.get("id").in(ids));

        // Set default sore as id of root entity if not provide
        if (Objects.equals(pageable.getSort(), Sort.unsorted())) {
            query.orderBy(builder.asc(root.get("id")));
        } else {
            query.orderBy(getOrderFromPageable(root, builder, pageable));
        }

        final var typedQuery = entityManager.createQuery(query);

        final var transformer = new GenericResultTransformer<>(clsDTO);
        final var tuples = typedQuery.getResultList();
        tuples.forEach(tuple -> transformer.transformTuple(tuple.toArray(),
                tuple.getElements().stream().map(TupleElement::getAlias).toArray(String[]::new)));
        final var content = transformer.transformList(new ArrayList<>());

        // Get total count
        final var countQuery = builder.createQuery(Long.class);
        final var countRoot = countQuery.from(clsEntity);
        final var countPredicate = spec.toPredicate(countRoot, countQuery, builder);
        countQuery.select(builder.countDistinct(countRoot));
        countQuery.where(countPredicate);

        final var total = entityManager.createQuery(countQuery).getSingleResult();

        return new CustomPage<>(content, pageable, total);
    }

    private List<Order> getOrderFromPageable(final Root<ET> root,
                                             final CriteriaBuilder builder,
                                             final Pageable pageable) {
        final var orders = new ArrayList<Order>();
        for (final var order : pageable.getSort()) {
            final var path = root.get(order.getProperty());
            final Order jpaOrder;
            if (order.isAscending()) {
                jpaOrder = builder.asc(path);
            } else {
                jpaOrder = builder.desc(path);
            }
            orders.add(jpaOrder);
        }
        return orders;
    }

    @Override
    public <DT> DT findByIdWithRelationshipProjection(final Long id,
                                                      final List<JoinConfig> joinConfigs,
                                                      final Class<ET> clsEntity,
                                                      final Class<DT> clsDTO) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createTupleQuery();
        final var root = query.from(clsEntity);

        // Dynamically build joins based on the provided list of join tables
        final var joins = new ArrayList<Join<?, ?>>();
        final var aliasType = joinConfigs.stream()
                .reduce(new HashMap<String, String>(), (m, c) -> {
                    m.put(c.getEntityPropertyName(), c.getDtoPropertyName());
                    return m;
                }, (m, c) -> {
                    m.putAll(c);
                    return m;
                });

        for (final var joinTable : joinConfigs) {
            // Example of mapping many-to-many, Student contains courseStudents and courseStudent contain student and course
            // So if root from student table it will join student with student_course and student_course with course
            final var joinTableParts = joinTable.getJoinPropertyName().split("\\.");
            Join<?, ?> join = root.join(joinTableParts[0], joinTable.getJoinType().getFirst());
            for (int i = 1; i < joinTableParts.length; i++) {
                join = join.join(joinTableParts[i], joinTable.getJoinType().get(i));
            }
            joins.add(join);
        }

        // Build the select clause for root entity
        final var selections = new ArrayList<Selection<?>>();
        final var fields = getSelectField(clsDTO);
        final var fieldJoinDTONames = joinConfigs.stream()
                .map(JoinConfig::getDtoPropertyName)
                .toList();
        for (final var field : fields) {
            if (!fieldJoinDTONames.contains(field.getName())) {
                selections.add(root.get(field.getName()).alias(field.getName()));
            }
        }

        // Build select clause for join table of dto class
        for (final var join : joins) {
            //final var fieldJoinEntityName = join.getModel().getBindableJavaType().getSimpleName().toLowerCase();
            final var fieldJoinEntityName = join.getAttribute().getName();
            final var joinFieldName = aliasType.get(fieldJoinEntityName);
            final var joinField = fields.stream()
                    .filter(f -> f.getName().equals(joinFieldName))
                    .findFirst().orElse(null);

            final List<Field> subFields;
            assert joinField != null;
            if (Collection.class.isAssignableFrom(joinField.getType())) {
                final var parameterType = (ParameterizedType) joinField.getGenericType();
                final var typeOfJoinField = (Class<?>) parameterType.getActualTypeArguments()[0];
                subFields = getSelectField(typeOfJoinField);
            } else {
                subFields = getSelectField(joinField.getType());
            }
            for (final var subField : subFields) {
                // Use alias format joinFieldName.fieldName for mapping result
                // Example Student contains many course so it will courses.id , courses.name etc
                selections.add(join.get(subField.getName()).alias(joinFieldName + "." + subField.getName()));
            }
        }

        query.multiselect(selections.toArray(new Selection<?>[0]));

        query.where(builder.equal(root.get("id"), id));

        final var typedQuery = entityManager.createQuery(query);

        final var transformer = new GenericResultTransformer<>(clsDTO);
        final var tuples = typedQuery.getResultList();
        tuples.forEach(tuple -> transformer.transformTuple(tuple.toArray(),
                tuple.getElements().stream().map(TupleElement::getAlias).toArray(String[]::new)));
        final var content = transformer.transformList(new ArrayList<>());

        if (Objects.isNull(content) || content.isEmpty()) {
            throw new ResourceNotFoundException(clsEntity.getSimpleName(), id);
        } else {
            return content.getFirst();
        }
    }
}
