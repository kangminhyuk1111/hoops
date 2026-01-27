package com.hoops.location.application.port.out;

import com.hoops.location.domain.model.Location;

import java.util.List;
import java.util.Optional;

/**
 * Location 영속성 포트 인터페이스
 */
public interface LocationRepositoryPort {
    Location save(Location location);

    Optional<Location> findById(Long id);

    boolean existsByName(String name);

    List<Location> findAll();

    List<Location> searchByKeyword(String keyword);
}
