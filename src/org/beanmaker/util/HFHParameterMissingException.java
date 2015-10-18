package org.beanmaker.util;

public class HFHParameterMissingException extends IllegalArgumentException {

    public HFHParameterMissingException(final String parameter) {
        super("Parameter '" + parameter + "' cannot be retrieved without having been set first.");
    }
}
