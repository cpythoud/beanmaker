package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ParametersBase {
    protected static final List<String> NAMING_FIELDS = new ArrayList<String>();
    protected static final List<String> ORDERING_FIELDS = new ArrayList<String>();

    public abstract String getOrderByFields();

    public List<String> getNamingFields() {
        return Collections.unmodifiableList(NAMING_FIELDS);
    }

    public List<String> getOrderingFields() {
        return Collections.unmodifiableList(ORDERING_FIELDS);
    }
}
