package org.sergei.backendJonasv2.config;

import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.sergei.backendJonasv2.application.pipeline.stages.EnrichAncestorRankStage;
import org.sergei.backendJonasv2.application.pipeline.stages.FetchBioSourceDetailsStage;
import org.sergei.backendJonasv2.application.pipeline.stages.FetchRemoteBioSourcesStage;
import org.sergei.backendJonasv2.application.pipeline.stages.FilterNewBioSourceStage;
import org.sergei.backendJonasv2.application.pipeline.stages.LoadBioSourcesStage;
import org.sergei.backendJonasv2.application.pipeline.stages.PersistBioSourceStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Hier wird die Reihenfolge der Stages festgelegt.
 * Spring würde sonst irgendeine Reihenfolge nehmen - das wäre gefährlich.
 */
@Configuration
public class PipelineConfig {

    @Bean
    public List<PipelineStage> syncBioSourcesStages(
            FetchRemoteBioSourcesStage fetchRemote,
            LoadBioSourcesStage loadLocal,
            FilterNewBioSourceStage filterNew,
            FetchBioSourceDetailsStage fetchDetails,
            EnrichAncestorRankStage enrichAncestor,
            PersistBioSourceStage persist
    ) {
        return List.of(
                fetchRemote,
                loadLocal,
                filterNew,
                fetchDetails,
                enrichAncestor,
                persist
        );
    }
}
