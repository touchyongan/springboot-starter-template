package io.touchyongan.starter_template.common.specification;

import io.touchyongan.starter_template.common.data.CustomPage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BaseProjectionRepository<ET> {

    <DT> DT findByIdWithSingleFieldProjection(final Long id,
                                              final Class<DT> clsResult,
                                              final Class<ET> clsEntity,
                                              final String fieldName);

    <DT> List<DT> findByIdWithListOfSingleFieldProjection(final Long id,
                                                          final Class<DT> clsResult,
                                                          final Class<ET> clsEntity,
                                                          final String fieldName);

    <DT> List<DT> findByIdWithListOfSingleFieldProjection(final Long id,
                                                          final Class<DT> clsResult,
                                                          final Class<ET> clsEntity,
                                                          final String fieldName,
                                                          final Sort.Direction direction);

    <DT> List<DT> findBySpecificationWithListOfSingleFieldProjection(final Specification<ET> specification,
                                                                     final Class<DT> clsResult,
                                                                     final Class<ET> clsEntity,
                                                                     final String fieldName);

    <DT> List<DT> findBySpecificationWithListOfSingleFieldProjection(final Specification<ET> specification,
                                                                     final Class<DT> clsResult,
                                                                     final Class<ET> clsEntity,
                                                                     final String fieldName,
                                                                     final Sort.Direction direction);

    <DT extends Number> DT aggregateByField(final Specification<ET> specification,
                                            final Class<DT> clsResult,
                                            final Class<ET> clsEntity,
                                            final String fieldName,
                                            final AggregateFunction aggregateFunction);

    <DT> DT findByIdWithProjection(Long id,
                                   Class<DT> clsDTO,
                                   Class<ET> clsEntity);

    <DT> List<DT> findAllByIdWithProjection(List<Long> ids,
                                            Class<DT> clsDTO,
                                            Class<ET> clsEntity);

    <DT> List<DT> findAllWithSpecification(Specification<ET> specification,
                                           Class<DT> clsDTO,
                                           Class<ET> clsEntity);

    <DT> List<DT> findAllWithSpecification(final Specification<ET> specification,
                                           final SortRequest sort,
                                           final Class<DT> clsDTO,
                                           final Class<ET> clsEntity);

    <DT> CustomPage<DT> findAllWithSpecification(Specification<ET> specification,
                                                 Pageable pageable,
                                                 Class<DT> clsDTO,
                                                 Class<ET> clsEntity);

    <DT> CustomPage<DT> findAllWithSpecificationWithRelationshipProjection(final Specification<ET> specification,
                                                                           final Pageable pageable,
                                                                           final List<JoinConfig> joinConfigs,
                                                                           final Class<ET> cslEntity,
                                                                           final Class<DT> clsDTO);

    <DT> DT findByIdWithRelationshipProjection(final Long id,
                                               final List<JoinConfig> joinConfigs,
                                               final Class<ET> clsEntity,
                                               final Class<DT> clsDTO);

    default Specification<ET> getDefualtSpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}
