package org.beanmaker.util;

public class DbBeanFileIdentityStoredFilenameCalculator implements DbBeanFileStoredFilenameCalculator {

    @Override
    public String calc(final String originalFilename) {
        return originalFilename;
    }
}
