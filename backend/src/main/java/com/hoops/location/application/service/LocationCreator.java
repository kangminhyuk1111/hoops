package com.hoops.location.application.service;

import com.hoops.location.application.exception.DuplicateLocationNameException;
import com.hoops.location.application.port.in.CreateLocationCommand;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.domain.model.Location;
import com.hoops.location.application.port.out.LocationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationCreator implements CreateLocationUseCase {

    private final LocationRepositoryPort locationRepository;

    @Override
    public Location createLocation(CreateLocationCommand command) {
        checkDuplicateName(command.name());

        Location location = Location.createNew(
                1L, // TODO userId는 추후 인증 기능 구현 시 추가
                command.name(),
                command.latitude(),
                command.longitude(),
                command.address()
        );

        return locationRepository.save(location);
    }

    private void checkDuplicateName(String name) {
        if (locationRepository.existsByName(name)) {
            throw new DuplicateLocationNameException(name);
        }
    }
}
