package org.beanmaker.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToStringMaker {

    private final String beanClass;
    private final long idBean;

    private final Map<String, String> fields = new LinkedHashMap<String, String>();

    public ToStringMaker(final DbBeanInterface bean) {
        beanClass = bean.getClass().getName();
        idBean = bean.getId();
    }

    public void addField(final String name, final String value) {
        if (value == null)
            fields.put(name, "null");
        else
            fields.put(name, value);
    }

    public void addField(final String name, final Object value) {
        if (value == null)
            fields.put(name, "null");
        else
            addField(name, value.toString());
    }

    public void addField(final String name, final boolean value) {
        addField(name, Boolean.toString(value));
    }

    public void addField(final String name, final int value) {
        addField(name, Integer.toString(value));
    }

    public void addField(final String name, final long value) {
        addField(name, Long.toString(value));
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[").append(beanClass).append(" #").append(idBean);

        for (Map.Entry mapEntry: fields.entrySet())
            buf.append(", ").append(mapEntry.getKey()).append("=").append(mapEntry.getValue());

        buf.append("]");
        return buf.toString();
    }
}
