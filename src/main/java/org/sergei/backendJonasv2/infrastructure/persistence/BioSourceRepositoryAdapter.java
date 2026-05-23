package org.sergei.backendJonasv2.infrastructure.persistence;

import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.domain.model.BioSource;
import org.sergei.backendJonasv2.domain.port.BioSourceRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class BioSourceRepositoryAdapter implements BioSourceRepositoryPort {

    @Override
    public List<BioSource> findAll() {
        // TODO: replace with real DB query (Spring Data JPA or JDBC)
        return List.of();
    }

    @Override
    public Optional<BioSource> findByEid(String eid) {
        // TODO: replace with real DB query
        return Optional.empty();
    }

    @Override
    public List<BioSource> saveAll(List<BioSource> bioSources) {
        log.info("Saving {} BioSources", bioSources.size());
        // TODO: replace with real DB insert/update
        return bioSources;
    }
}
