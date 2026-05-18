package org.sergei.backendJonasv2.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.sergei.backendJonasv2.domain.exception.BioSourceNotFoundException;
import org.sergei.backendJonasv2.domain.exception.PipelineException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Zentrale Fehlerbehandlung - keine try/catch mehr in Controllern.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "message", message
        ));
    }

    @ExceptionHandler(BioSourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(BioSourceNotFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PipelineException.class)
    public ResponseEntity<Map<String, Object>> handlePipelineError(PipelineException e) {
        log.error("Pipeline-Fehler", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<Map<String, Object>> handleGeneric(Exception e){
        log.error("Unerwarteter Fehler", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Interner Fehler");
    }


}
