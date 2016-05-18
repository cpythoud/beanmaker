package org.beanmaker.util;

public interface ProtectedIdInterface extends DbBeanInterface {

    ProtectedIdManager getProtectedIdManager();

    String getProtectionCode();

    void initFromProtectionCode(final String code);
}
