package org.beanmaker.util;

import java.util.List;
import java.util.Locale;

public interface DbBeanInterface {

    public void setId(final long id);

    public void resetId();

    public void updateDB();

    public void preUpdateConversions();

    public boolean isDataOK();

    public List<ErrorMessage> getErrorMessages();

    public void reset();

    public void fullReset();

    public void delete();

    public void setLocale(final Locale locale);
}
