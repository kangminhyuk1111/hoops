package com.hoops.participation.infrastructure.adapter;

import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.participation.infrastructure.ParticipationEntity;
import com.hoops.participation.infrastructure.jpa.JpaParticipationRepository;
import com.hoops.participation.infrastructure.mapper.ParticipationMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ParticipationRepositoryImpl implements ParticipationRepository {

    private final JpaParticipationRepository jpaParticipationRepository;

    public ParticipationRepositoryImpl(JpaParticipationRepository jpaParticipationRepository) {
        this.jpaParticipationRepository = jpaParticipationRepository;
    }

    @Override
    public Participation save(Participation participation) {
        ParticipationEntity entity = ParticipationMapper.toEntity(participation);
        ParticipationEntity savedEntity = jpaParticipationRepository.save(entity);
        return ParticipationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Participation> findById(Long id) {
        return jpaParticipationRepository.findById(id).map(ParticipationMapper::toDomain);
    }
}
