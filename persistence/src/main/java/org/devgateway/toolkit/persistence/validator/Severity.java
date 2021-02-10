package org.devgateway.toolkit.persistence.validator;

import javax.validation.Payload;

/**
 * @author Octavian Ciubotaru
 */
public class Severity {

    /**
     * Used in cases when the object cannot be accepted in its current form. For example when tender document is
     * added to a tender process which already has a tender document.
     */
    public interface NonRecoverable extends Payload {
    }
}
