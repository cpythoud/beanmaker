package org.beanmaker.util;

import org.jcodegen.html.DivTag;

import java.util.ResourceBundle;

public interface HtmlDefaultButtonsHelper {

    DivTag getDefaultDialogButtons(
            final HtmlFormHelper htmlFormHelper,
            final long id,
            final String name,
            final ResourceBundle resourceBundle);

    DivTag getDefaultDialogButtons(
            final HtmlFormHelper htmlFormHelper,
            final long id,
            final String name,
            final ResourceBundle resourceBundle,
            final boolean submitDisabled);

    DivTag getLonelyCloseButton(final ResourceBundle resourceBundle);

}
