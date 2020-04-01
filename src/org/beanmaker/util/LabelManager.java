package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

import java.util.List;
import java.util.Map;

public interface LabelManager {

    DbBeanLabel get(final long id);

    boolean isIdOK(final long id);

    boolean isIdOK(final long id, final DBTransaction transaction);

    String get(final long id, final DbBeanLanguage dbBeanLanguage);

    String get(final String name, final DbBeanLanguage dbBeanLanguage);

    DbBeanLabel createInstance();

    List<DbBeanLanguage> getAllActiveLanguages();

    DbBeanLanguage getLanguage(final long id);

    DbBeanLanguage getCopy(final DbBeanLanguage dbBeanLanguage);

    DbBeanLabel replaceData(final DbBeanLabel into, final DbBeanLabel from);

    Map<String, String> getLabelMap(final DbBeanLanguage dbBeanLanguage, final String... labelNames);

    Map<String, String> getLabelMap(final DbBeanLanguage dbBeanLanguage, final List<String> labelNames);
}
