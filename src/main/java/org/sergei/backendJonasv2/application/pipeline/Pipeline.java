package org.sergei.backendJonasv2.application.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Führt eine Liste von Stages der Reihe nach aus.
 * <p>
 * Wenn eine Stage fehlschlägt:
 * - Pipeline stoppt (Fail-Fast Strategie)
 * - Fehler wird in PipelineResult verpackt.
 */

@Slf4j
@RequiredArgsConstructor
public class Pipeline {

    private final List<PipelineStage> stages;

    public PipelineResult run(PipelineContext context) {
        log.info("Pipeline gestartet mit {} Phasen", stages.size());

        for (PipelineStage stage : stages) {
            if (!stage.shouldRun(context)) {
                log.info("Phase übersprungen: {}", stage.getName());
                continue;
            }

            try {
                long startTime = System.currentTimeMillis();
                log.info("Phase startet: {}", stage.getName());

                stage.execute(context);

                long duration = System.currentTimeMillis() - startTime;
                log.info("Phase fertig: {} ({} ms)", stage.getName(), duration);

            } catch (Exception e) {
                log.error("Phase fehlgeschlagen: {} ", stage.getName(), e);
                return PipelineResult.failure(
                        context,
                        stage.getName(),
                        e.getMessage()
                );
            }
        }

        log.info("Pipeline erfolgreich beendet");
        return PipelineResult.success(context);

    }
}
