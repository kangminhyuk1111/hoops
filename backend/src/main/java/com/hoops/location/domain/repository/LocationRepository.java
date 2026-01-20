package com.hoops.location.domain.repository;

import com.hoops.location.domain.model.Location;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    Location save(Location location);

    Optional<Location> findById(Long id);

    boolean existsByName(String name);

    List<Location> findAll();

    List<Location> searchByKeyword(String keyword);
}
