package org.beanmaker.util;

public class MissingImplementationException extends RuntimeException {

    public MissingImplementationException(final String origin) {
        super("Missing implementation for: " + origin);
    }
}
