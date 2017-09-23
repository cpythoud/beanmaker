package org.beanmaker.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class BaseView {
    final private String resourceBundleName;
    protected ResourceBundle resourceBundle;
    protected Locale locale;

    public BaseView(final String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
        initLocale(Locale.getDefault());
    }

    private void initLocale(final Locale locale) {
        this.locale = locale;
        resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale);
    }

    public void setLocale(final Locale locale) {
        initLocale(locale);
    }
}
