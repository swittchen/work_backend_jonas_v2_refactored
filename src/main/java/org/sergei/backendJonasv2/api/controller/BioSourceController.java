package org.sergei.backendJonasv2.api.controller;

import lombok.RequiredArgsConstructor;
import org.sergei.backendJonasv2.api.dto.SyncResponse;
import org.sergei.backendJonasv2.application.pipeline.PipelineResult;
import org.sergei.backendJonasv2.application.usecase.SyncBioSourcesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller - DÜNN!
 * Nur Endpoint-Definition + Aufruf des Use Cases.
 * KEINE Geschäftslogik
 */

@RestController
@RequestMapping("/api/v1/biosources")
@RequiredArgsConstructor
public class BioSourceController {

    private final SyncBioSourcesUseCase syncUseCase;

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> synchronize() {
        PipelineResult result = syncUseCase.execute();

        if (!result.success()) {
            return ResponseEntity
                    .internalServerError()
                    .body(SyncResponse.failed(result.failedStageName(), result.errorMessage()));
        }

        return ResponseEntity.ok(SyncResponse.from(result.context()));
    }
}
