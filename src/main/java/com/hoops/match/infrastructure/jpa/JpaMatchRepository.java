package com.hoops.match.infrastructure.jpa;

import com.hoops.match.infrastructure.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMatchRepository extends JpaRepository<MatchEntity, Long> {
}
