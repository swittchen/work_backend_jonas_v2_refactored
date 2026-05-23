package org.sergei.backendJonasv2.api.dto;

import org.sergei.backendJonasv2.application.pipeline.PipelineContext;

import java.time.Instant;
import java.util.List;

public record SyncResponse(
        boolean success,
        int syncedCount,
        List<String> warnings,
        String failedStage,
        String errorMessage,
        Instant timestamp
) {
    public static SyncResponse from(PipelineContext context) {
        return new SyncResponse(
                true,
                context.getReadyToPersist().size(),
                context.getWarnings(),
                null,
                null,
                Instant.now()
        );
    }

    public static SyncResponse failed(String stageName, String errorMessage) {
        return new SyncResponse(false, 0, List.of(), stageName, errorMessage, Instant.now());
    }
}
