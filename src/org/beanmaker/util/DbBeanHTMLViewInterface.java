package org.beanmaker.util;

import javax.servlet.ServletRequest;

import java.util.List;
import java.util.Locale;

public interface DbBeanHTMLViewInterface {

    public void setLocale(final Locale locale);

    public List<ErrorMessage> getErrorMessages();

    public void resetId();

    public void setResetId(final String dummy);

    public void setId(final long id);

    public String getHtmlForm();

    public void setAllFields(final ServletRequest request);

    public boolean isDataOK();

    public void updateDB();

    public void setUpdateDB(final String dummy);

    public void reset();

    public void fullReset();

    public void setReset(final String dummy);

    public void setFullReset(final String dummy);

    public void delete();

    public void setDelete(final String dummy);
}
