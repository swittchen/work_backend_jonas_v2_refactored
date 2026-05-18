package org.sergei.backendJonasv2.domain.port;

import org.sergei.backendJonasv2.domain.model.BioSource;

import java.util.List;
import java.util.Optional;

/**
 * Port - definiert WAS gebraucht wird, nicht WIE es funktioniert.
 * Die Implementierung sitzt in infrastructure (Adapter).
 */
public interface BioSourceRepositoryPort {
    List<BioSource> findAll();

    Optional<BioSource> findByEid(String eid);

    List<BioSource> saveAll(List<BioSource> nioSources);
}
