package org.sergei.backendJonasv2.application.pipeline.stages;

import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.application.pipeline.PipelineContext;
import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.sergei.backendJonasv2.domain.model.BioSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EnrichAncestorRankStage implements PipelineStage {

    @Override
    public String getName() {
        return "EnrichAncestorRank";
    }

    @Override
    public void execute(PipelineContext context) {
        List<BioSource> enriched = context.getFetchedDetails().stream()
                .filter(b -> b.getAncestorEid() != null)
                .collect(Collectors.toList());

        int skipped = context.getFetchedDetails().size() - enriched.size();
        if (skipped > 0) {
            context.addWarning(skipped + " BioSources ohne ancestorEid übersprungen");
        }

        context.setReadyToPersist(enriched);
        log.info("{} BioSources angereichert", enriched.size());
    }
}
