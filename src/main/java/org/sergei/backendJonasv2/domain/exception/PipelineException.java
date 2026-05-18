package org.sergei.backendJonasv2.domain.exception;

/**
 * Basis_exception für alle Pipeline-Fehler.
 * Wir benutzen RuntimeException als Parent, damit wir keine
 * cheked exceptions durch alle Methoden schleppen müssen.
 */
public class PipelineException extends RuntimeException {
    private final String stageName;


    public PipelineException(String stageName, String message) {
        super(message);
        this.stageName = stageName;
    }

    public PipelineException(String stageName, String message, Throwable cause) {
        super(message, cause);
        this.stageName = stageName;
    }

    public String getStageName() {
        return stageName;
    }
}
