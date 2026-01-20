package com.hoops.location.application.service;

import com.hoops.location.application.exception.DuplicateLocationNameException;
import com.hoops.location.domain.exception.InvalidLocationNameException;
import com.hoops.location.application.port.in.CreateLocationCommand;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationCreator implements CreateLocationUseCase {

    private static final int MIN_NAME_LENGTH = 2;

    private final LocationRepository locationRepository;

    @Override
    public Location createLocation(CreateLocationCommand command) {
        validateLocationName(command.name());
        checkDuplicateName(command.name());

        Location location = new Location(
                null,
                1L, // TODO userId는 추후 인증 기능 구현 시 추가
                command.name(),
                command.latitude(),
                command.longitude(),
                command.address()
        );

        return locationRepository.save(location);
    }

    private void validateLocationName(String name) {
        if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
            throw new InvalidLocationNameException(name);
        }
    }

    private void checkDuplicateName(String name) {
        if (locationRepository.existsByName(name)) {
            throw new DuplicateLocationNameException(name);
        }
    }
}
