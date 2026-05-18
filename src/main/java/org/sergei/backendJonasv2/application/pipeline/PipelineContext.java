package org.sergei.backendJonasv2.application.pipeline;

import lombok.Getter;
import lombok.Setter;
import org.sergei.backendJonasv2.domain.model.BioSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Geteilter Zustand zwischen Pipeline-Phasen.
 * Jede Stage liest Eingaben und schreibt Ergebnisse hierhin.
 * <p>
 * Wir benutzen ein dedizierted Objekt statt einer Map<String, Object>,
 * weil so Typsicherheit erhalten bleibt (Complier-Checks statt Casting )
 */

@Getter
@Setter
public class PipelineContext {

    // Rohdaten vom externen API (Schritt 1)
    private List<BioSource> remoteBioSources = new ArrayList<>();

    // Bereits in der DB existierende BioSources ( Schritt 2)
    private List<BioSource> localBioSources = new ArrayList<>();

    // EIDs die wir noch holen müssen ( Schritt 3)
    private List<String> newEids = new ArrayList<>();

    // Komplette Details der neuen BioSources (Schritt 4)
    private List<BioSource> fetchedDetails = new ArrayList<>();

    // Fertig angereicherte Entities (Schritt 5)
    private List<BioSource> readyToPersist = new ArrayList<>();

    // Gespeicherte Entites (Schritt 6)
    private Map<String, Object> metadata = new HashMap<>();
    private List<String> warnings = new ArrayList<>();

    public void addWarning(String message) {
        this.warnings.add(message);
    }
}
