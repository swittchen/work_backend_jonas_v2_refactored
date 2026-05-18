package org.sergei.backendJonasv2.domain.port;

import org.sergei.backendJonasv2.domain.model.BioSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port für externe API-Calls - asynchron mit Reactor
 */
public interface BioSourceRemoteClientPort {
    Flux<BioSource> fetchAllChildren();

    Flux<BioSource> fetchDetailsByEids(List<String> eids);

    Mono<String> createBioSource(BioSource bioSource);
}
