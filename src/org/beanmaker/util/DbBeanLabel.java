package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

import java.util.List;

public interface DbBeanLabel {

    void setId(final long id);
    long getId();

    String get(final DbBeanLanguage dbBeanLanguage);
    String get(final DbBeanLanguage dbBeanLanguage, final Object... parameters);
    String get(final DbBeanLanguage dbBeanLanguage, final List<Object> parameters);

    void cacheLabelsFromDB();
    void clearCache();

    void updateLater(final DbBeanLanguage dbBeanLanguage, final String text);

    long updateDB(final DBTransaction transaction);
    void commitTextsToDatabase(final DBTransaction transaction);

    boolean hasDataFor(final DbBeanLanguage dbBeanLanguage);

    void reset();
    void fullReset();
}
