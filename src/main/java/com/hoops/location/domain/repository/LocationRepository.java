package com.hoops.location.domain.repository;

import com.hoops.location.domain.Location;
import java.util.Optional;

public interface LocationRepository {
    Location save(Location location);

    Optional<Location> findById(Long id);

    boolean existsByName(String name);
}
