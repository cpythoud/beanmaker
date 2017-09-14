package org.beanmaker.util;

public interface DbBeanMultilingual {

    long getId();

    long getIdLabel();
    DbBeanLabel getLabel();

    String getIdLabelLabel();

    String getLabel(final DbBeanLanguage dbBeanLanguage);
}
