package org.sergei.backendJonasv2.application.pipeline.stages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.application.pipeline.PipelineContext;
import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.sergei.backendJonasv2.domain.port.BioSourceRepositoryPort;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistBioSourceStage implements PipelineStage {

    private final BioSourceRepositoryPort repository;

    @Override
    public String getName() {
        return "PersistBioSources";
    }

    @Override
    public boolean shouldRun(PipelineContext context) {
        return !context.getReadyToPersist().isEmpty();
    }

    @Override
    public void execute(PipelineContext context) {
        repository.saveAll(context.getReadyToPersist());
        log.info("{} BioSources persistiert", context.getReadyToPersist().size());
    }
}
