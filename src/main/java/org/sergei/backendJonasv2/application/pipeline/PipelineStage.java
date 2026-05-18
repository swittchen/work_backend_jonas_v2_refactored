package org.sergei.backendJonasv2.application.pipeline;

/**
 * Vertrag (Contract) für jede Pipeline-Phase.
 * <p>
 * Eine Stage:
 * - bekommt den aktuellen Context
 * - macht GENAU EINE Aufgabe (Single Responsibility)
 * - aktualisiert den Context
 * - kann durch Exception abbrechnen - Pipeline fängt das ab
 */

public interface PipelineStage {

    /**
     * Eindeutiger Name der Pahse  -  wird fürs Logging benutzt.
     */

    String getName();

    /**
     * Führt die Pahse aus.
     *
     * @param context geteilter Pipeline-Zustand
     */
    void execute(PipelineContext context);

    /**
     * Optional: Prüft, ob diese Phase überhaupt laufen soll.
     * Default = immer laufen.
     */
     default boolean shouldRun(PipelineContext context) {
        return true;
    }
}
