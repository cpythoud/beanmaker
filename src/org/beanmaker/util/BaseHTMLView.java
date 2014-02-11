package org.beanmaker.util;

import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class BaseHTMLView {
    final private String resourceBundleName;
    protected ResourceBundle resourceBundle;
    protected Locale locale;

    protected List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();

    protected boolean captchaControl = false;
    protected String captchaValue = "";
    protected String captchaControlValue = "";

    protected HtmlFormHelper htmlFormHelper = new HtmlFormHelper();
    protected ServletContext servletContext = null;

    public BaseHTMLView(final String resourceBundleName) {
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

    public void setCaptchaControl(final boolean captchaControl) {
        this.captchaControl = captchaControl;
    }

    public void setCaptchaValue(final String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public void setCaptchaControlValue(final String captchaControlValue) {
        this.captchaControlValue = captchaControlValue;
    }

    public String getInvalidCaptchaErrorMessage() {
        return resourceBundle.getString("invalid_captcha");

    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
