package org.beanmaker.util;

import java.util.List;
import java.util.Locale;

public interface DbBeanInterface {

    void setId(final long id);

    void resetId();

    void updateDB();

    void preUpdateConversions();

    boolean isDataOK();

    List<ErrorMessage> getErrorMessages();

    void reset();

    void fullReset();

    void delete();

    void setLocale(final Locale locale);
}
