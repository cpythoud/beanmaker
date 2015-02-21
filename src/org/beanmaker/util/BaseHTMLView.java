package org.beanmaker.util;

import javax.servlet.ServletContext;

public class BaseHTMLView extends BaseEditableView {

    protected boolean captchaControl = false;
    protected String captchaValue = "";
    protected String captchaControlValue = "";

    protected HtmlFormHelper htmlFormHelper = new HtmlFormHelper();
    protected ServletContext servletContext = null;

    public BaseHTMLView(final String resourceBundleName) {
        super(resourceBundleName);
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
