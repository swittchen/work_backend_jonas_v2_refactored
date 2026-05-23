package org.sergei.backendJonasv2.domain.exception;

public class BioSourceNotFoundException extends RuntimeException {
    public BioSourceNotFoundException(String eid) {
        super("BioSource not found: " + eid);
    }
}
