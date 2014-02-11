package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class BaseView {
    final private String resourceBundleName;
    protected ResourceBundle resourceBundle;
    protected Locale locale;

    protected List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();

    public BaseView(final String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
        setLocale(Locale.getDefault());
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
        resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale);
    }

    public List<ErrorMessage> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }
}
