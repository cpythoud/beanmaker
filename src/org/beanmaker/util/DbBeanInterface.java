package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

import java.util.List;
import java.util.Locale;

public interface DbBeanInterface {

    void setId(final long id);

    long getId();

    void resetId();

    void updateDB();
    long updateDB(final DBTransaction transaction);

    void preUpdateConversions();

    boolean isDataOK();

    List<ErrorMessage> getErrorMessages();

    void reset();

    void fullReset();

    void delete();

    void setLocale(final Locale locale);
}
