package org.sergei.backendJonasv2.application.pipeline;

/**
 * Result-Objekt - vermeidet null-Returns und macht
 * Erfolg/Fehler explizit.
 */


public record PipelineResult(
        boolean success,
        PipelineContext context,
        String failedStageName,
        String errorMessage
) {
    public static PipelineResult success(PipelineContext context) {
        return new PipelineResult(true, context, null, null);
    }

    public static PipelineResult failure(PipelineContext context, String failedStage, String error) {
        return new PipelineResult(false, context, failedStage, error);
    }
}
