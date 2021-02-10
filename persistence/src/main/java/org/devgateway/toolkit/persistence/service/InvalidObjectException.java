package org.devgateway.toolkit.persistence.service;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Exception thrown then an object did not pass Bean Validation Framework validation.
 *
 * @author Octavian Ciubotaru
 */
public class InvalidObjectException extends RuntimeException {

    private final Set<ConstraintViolation<?>> violations;

    public InvalidObjectException(Set<ConstraintViolation<?>> violations) {
        this.violations = violations;
    }

    public Set<ConstraintViolation<?>> getViolations() {
        return violations;
    }
}
