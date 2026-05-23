package org.sergei.backendJonasv2.application.pipeline.stages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.application.pipeline.PipelineContext;
import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.sergei.backendJonasv2.domain.model.BioSource;
import org.sergei.backendJonasv2.domain.port.BioSourceRemoteClientPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchRemoteBioSourcesStage implements PipelineStage {

    private final BioSourceRemoteClientPort remoteClient;

    @Override
    public String getName() {
        return "FetchRemoteBioSources";
    }

    @Override
    public void execute(PipelineContext context) {
        List<BioSource> remote = remoteClient.fetchAllChildren().collectList().block();
        context.setRemoteBioSources(remote != null ? remote : List.of());
        log.info("{} BioSources vom Remote geladen", context.getRemoteBioSources().size());
    }
}
