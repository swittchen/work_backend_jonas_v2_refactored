package org.sergei.backendJonasv2.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Synchronisiert BioSources zwischen Signals und lokaler DB.
 * Hier wird die Pipeline nur ZUSAMMENGESTELLT und AUSGEFÜHRT.
 * Die Logik selbst lebt in den Stages.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBioSourcesUseCase {
}
