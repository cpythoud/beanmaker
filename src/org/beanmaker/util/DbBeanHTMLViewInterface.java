package org.beanmaker.util;

import org.jcodegen.html.FormTag;

import javax.servlet.ServletRequest;

import java.util.List;
import java.util.Locale;

public interface DbBeanHTMLViewInterface {

    void setLocale(final Locale locale);

    List<ErrorMessage> getErrorMessages();

    void resetId();

    void setResetId(final String dummy);

    void setId(final long id);

    String getHtmlForm();

    FormTag getHtmlFormTag();

    void setAllFields(final ServletRequest request);

    boolean isDataOK();

    void updateDB();

    void setUpdateDB(final String dummy);

    void reset();

    void fullReset();

    void setReset(final String dummy);

    void setFullReset(final String dummy);

    void delete();

    void setDelete(final String dummy);
}
