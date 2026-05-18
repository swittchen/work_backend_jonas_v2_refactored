package org.sergei.backendJonasv2.config;

import org.sergei.backendJonasv2.application.pipeline.PipelineStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Heir wird die Reihenfolge der Stages festgelegt.
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
