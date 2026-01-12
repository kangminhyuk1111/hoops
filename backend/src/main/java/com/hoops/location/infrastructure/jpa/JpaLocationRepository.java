package com.hoops.location.infrastructure.jpa;

import com.hoops.location.infrastructure.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaLocationRepository extends JpaRepository<LocationEntity, Long> {

    boolean existsByAlias(String alias);

    @Query("SELECT l FROM LocationEntity l WHERE l.alias LIKE %:keyword% OR l.address LIKE %:keyword%")
    List<LocationEntity> searchByKeyword(@Param("keyword") String keyword);
}
