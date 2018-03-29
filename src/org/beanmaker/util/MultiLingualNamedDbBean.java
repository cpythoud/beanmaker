package org.beanmaker.util;

public interface MultiLingualNamedDbBean extends DbBeanInterface {

    String getDisplayName(final DbBeanLanguage dbBeanLanguage);
}
