package com.hoops.location.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataLocationRepository extends JpaRepository<LocationJpaEntity, Long> {

    boolean existsByAlias(String alias);

    @Query("SELECT l FROM LocationJpaEntity l WHERE l.alias LIKE %:keyword% OR l.address LIKE %:keyword%")
    List<LocationJpaEntity> searchByKeyword(@Param("keyword") String keyword);
}
