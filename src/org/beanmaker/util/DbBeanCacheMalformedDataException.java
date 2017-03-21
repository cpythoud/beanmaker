package org.beanmaker.util;

public class DbBeanCacheMalformedDataException extends IllegalArgumentException {

    private final String cacheCode;

    public DbBeanCacheMalformedDataException(final String message) {
        super(message);
        cacheCode = null;
    }

    public DbBeanCacheMalformedDataException(final String message, final Throwable cause) {
        super(message, cause);
        cacheCode = null;
    }

    public DbBeanCacheMalformedDataException(final Throwable cause) {
        super(cause);
        cacheCode = null;
    }

    public DbBeanCacheMalformedDataException(final String message, final String cacheCode) {
        super(message);
        this.cacheCode = cacheCode;
    }

    public String getCacheCode() {
        return cacheCode;
    }
}
