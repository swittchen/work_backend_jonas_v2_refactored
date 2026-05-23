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
public class LoadBioSourcesStage implements PipelineStage {

    private final BioSourceRepositoryPort repository;

    @Override
    public String getName() {
        return "LoadLocalBioSources";
    }

    @Override
    public void execute(PipelineContext context) {
        context.setLocalBioSources(repository.findAll());
        log.info("{} BioSources aus der DB geladen", context.getLocalBioSources().size());
    }
}
