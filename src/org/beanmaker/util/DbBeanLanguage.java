package org.beanmaker.util;

import java.util.Locale;

public interface DbBeanLanguage {

    long getId();

    String getName();

    String getIso();
    String getCapIso();

    Locale getLocale();
}
