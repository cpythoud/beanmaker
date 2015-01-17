package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseEditableView extends BaseView {

    protected List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();

    public BaseEditableView(final String resourceBundleName) {
        super(resourceBundleName);
    }

    public List<ErrorMessage> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }
}
