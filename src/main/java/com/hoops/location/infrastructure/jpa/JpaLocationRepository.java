package com.hoops.location.infrastructure.jpa;

import com.hoops.location.infrastructure.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLocationRepository extends JpaRepository<LocationEntity, Long> {

    boolean existsByAlias(String alias);
}
