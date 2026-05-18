package org.sergei.backendJonasv2.infrastructure.http.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.domain.model.BioSource;
import org.sergei.backendJonasv2.domain.port.BioSourceRemoteClientPort;
import org.sergei.backendJonasv2.infrastructure.http.mapper.SignalsResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * Adapter - implementiert den Domian-Port mit WebClient.
 * Hier passiert nur HTTP - KEINE Geschäftslogik.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalsWebClientAdapter implements BioSourceRemoteClientPort {

    private static final String BIO_SOURCE_LIBRARY_EID = "assetType:69171dcc6da72b77e913daa5";
    private static final int PAGE_SIZE = 100;

    private final WebClient signalsWebClient;
    private final SignalsResponseMapper responseMapper;

    private record PagedResponse(
            List<BioSource> items,
            int offset,
            boolean hasMore) {
    }

    private Mono<PagedResponse> fetchPage(int offset) {
        return signalsWebClient.get()
                .uri(uri -> uri
                        .path("/entities/{eid}/children")
                        .queryParam("page[offset]", offset)
                        .queryParam("page[limit]", PAGE_SIZE)
                        .build(BIO_SOURCE_LIBRARY_EID))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> responseMapper.toPagedResponse(json, offset, PAGE_SIZE));
    }

    private Mono<BioSource> fetchSingleBioSource(String eid) {
        return signalsWebClient.get()
                .uri("/materials/{eid}", eid)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(responseMapper::toBioSource)
                .doOnError(e -> log.error("Fehler bei EID {}: {}", eid, e.getLocalizedMessage()));
    }

    @Override
    public Flux<BioSource> fetchAllChildren() {
        //Reaktive Pagination - holt alle Seiten nacheinander
        return fetchPage(0)
                .expand(response -> {
                    int currentOffset = response.offset() + PAGE_SIZE;
                    if (response.hasMore()) {
                        return fetchPage(currentOffset);
                    }
                    return Mono.empty();
                })
                .flatMapIterable(PagedResponse::items);
    }

    @Override
    public Flux<BioSource> fetchDetailsByEids(List<String> eids) {
        return Flux.fromIterable(eids)
                .flatMap(this::fetchSingleBioSource, 5); //parallel mit limit 5
    }

    @Override
    public Mono<String> createBioSource(BioSource bioSource) {
        // Payload_building wird in einem separaten Mapper ausgelagert
        String payload = responseMapper.toCreateRequestPayload(bioSource);

        return signalsWebClient.post()
                .uri("/materials/Bio%20Sources/assets")
                .header("Content-Type", "application/vnd.api+json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Fehler beim Erstellen der BioSource: {}", e.getMessage()));
    }


}
