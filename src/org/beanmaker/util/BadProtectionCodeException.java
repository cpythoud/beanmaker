package org.beanmaker.util;

public class BadProtectionCodeException extends RuntimeException {

    public BadProtectionCodeException(final ProtectedIdInterface bean) {
        super("Missing protection code for bean " + bean.getClass().getName() + " #" + bean.getId());
    }

    public BadProtectionCodeException(final ProtectedIdInterface bean, final String code) {
        super("Bad protection code passed for bean " + bean.getClass().getName()
                + " #" + bean.getId()
                + (code == null ? "" : " (code = " + code + ")"));
    }

    public BadProtectionCodeException(final String message) {
        super(message);
    }
}
