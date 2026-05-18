package org.sergei.backendJonasv2.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.application.pipeline.Pipeline;
import org.sergei.backendJonasv2.application.pipeline.PipelineContext;
import org.sergei.backendJonasv2.application.pipeline.PipelineResult;
import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case: Synchronisiert BioSources zwischen Signals und lokaler DB.
 * Hier wird die Pipeline nur ZUSAMMENGESTELLT und AUSGEFÜHRT.
 * Die Logik selbst lebt in den Stages.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBioSourcesUseCase {

    //Spring injiziert alle Stages - Reihenfolge wird in der Config gesetzt
    private final List<PipelineStage> orderedStages;

    public PipelineResult execute() {
        log.info("Use Case 'SyncBioSources' gestartet");

        PipelineContext context = new PipelineContext();
        Pipeline pipeline = new Pipeline(orderedStages);

        return pipeline.run(context);
    }
}
