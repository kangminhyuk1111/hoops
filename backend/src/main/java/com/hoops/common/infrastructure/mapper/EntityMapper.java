package com.hoops.common.infrastructure.mapper;

public interface EntityMapper<D, E> {
    D toDomain(E entity);

    E toEntity(D domain);
}
