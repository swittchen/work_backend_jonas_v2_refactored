package org.sergei.backendJonasv2.application.pipeline.stages;

import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.application.pipeline.PipelineContext;
import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FilterNewBioSourceStage implements PipelineStage {

    @Override
    public String getName() {
        return "FilterNewBioSources";
    }

    @Override
    public void execute(PipelineContext context) {
        Set<String> localEids = context.getLocalBioSources().stream()
                .map(b -> b.getEid())
                .collect(Collectors.toSet());

        List<String> newEids = context.getRemoteBioSources().stream()
                .map(b -> b.getEid())
                .filter(eid -> !localEids.contains(eid))
                .collect(Collectors.toList());

        context.setNewEids(newEids);
        log.info("{} neue EIDs gefunden", newEids.size());
    }
}
