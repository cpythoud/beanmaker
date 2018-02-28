package org.beanmaker.util;

import java.util.Locale;

public interface DbBeanLanguage extends DbBeanInterface {

    String getName();

    String getIso();
    String getCapIso();

    Locale getLocale();
}
