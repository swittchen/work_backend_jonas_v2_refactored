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
public class FetchBioSourceDetailsStage implements PipelineStage {

    private final BioSourceRemoteClientPort remoteClient;

    @Override
    public String getName() {
        return "FetchBioSourceDetails";
    }

    @Override
    public boolean shouldRun(PipelineContext context) {
        return !context.getNewEids().isEmpty();
    }

    @Override
    public void execute(PipelineContext context) {
        List<BioSource> details = remoteClient.fetchDetailsByEids(context.getNewEids())
                .collectList()
                .block();
        context.setFetchedDetails(details != null ? details : List.of());
        log.info("{} BioSource-Details geladen", context.getFetchedDetails().size());
    }
}
