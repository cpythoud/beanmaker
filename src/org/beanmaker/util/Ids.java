package org.beanmaker.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Ids {

    public static String getProtectionCodeFromParameterName(final String parameterName) {
        return getProtectionCodeFromParameterName(parameterName, "_");
    }

    public static String getProtectionCodeFromParameterName(final String parameterName, final String separatorRegex) {
        final String[] elements = parameterName.split(separatorRegex);
        if (elements.length < 2)
            throw new IllegalArgumentException("Could not separate id/code from name based on regex: "
                    + separatorRegex);

        return elements[elements.length - 1];
    }

    public static long getIdFromParameterName(final String parameterName) {
        return Long.valueOf(getProtectionCodeFromParameterName(parameterName));
    }

    public static long getIdFromParameterName(final String parameterName, final String separatorRegex) {
        return Long.valueOf(getProtectionCodeFromParameterName(parameterName, separatorRegex));
    }

    public static Set<Long> getIdSet(final Collection<DbBeanInterface> beans) {
        final Set<Long> set = new HashSet<Long>();

        for (DbBeanInterface bean: beans)
            set.add(bean.getId());

        return set;
    }
}
