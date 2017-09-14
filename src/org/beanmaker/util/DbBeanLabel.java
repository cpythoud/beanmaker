package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

public interface DbBeanLabel {

    void setId(final long id);
    long getId();

    String get(final DbBeanLanguage dbBeanLanguage);

    void cacheLabelsFromDB();
    void clearCache();

    void updateLater(final DbBeanLanguage dbBeanLanguage, final String text);

    long updateDB(final DBTransaction transaction);
    void commitTextsToDatabase(final DBTransaction transaction);

    void reset();
    void fullReset();
}
