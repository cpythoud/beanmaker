package org.beanmaker.util;

public interface MultilingualIdNamePairBean {

    long getId();

    String getNameForPair(final DbBeanLanguage dbBeanLanguage);
}
