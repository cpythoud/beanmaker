package org.beanmaker.util;

import org.dbbeans.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    public static <T extends DbBeanInterface> Set<Long> getIdSet(final Collection<T> beans) {
        return getIdSet(beans, new HashSet<Long>());
    }

    public static <T extends DbBeanInterface> Set<Long> getIdSet(final Collection<T> beans, final Set<Long> set) {
        for (DbBeanInterface bean: beans)
            set.add(bean.getId());

        return set;
    }

    public static <T extends DbBeanInterface> String getAggregatedIdString(
            final Collection<T> beans,
            final String separator)
    {
        final List<Long> ids = new ArrayList<Long>();

        ids.addAll(getIdSet(beans));
        Collections.sort(ids);

        return Strings.concatWithSeparator(separator, Strings.asListOfStrings(ids));
    }

    public static Set<Long> getIdSet(final String aggregatedIds, final String separator, final boolean lenient) {
        final Set<Long> set = new HashSet<Long>();

        int index = 0;
        for (String val: aggregatedIds.split(separator)) {
            ++index;
            final long id = Strings.getLongVal(val);
            if (id == 0) {
                if (!lenient)
                    throw new IllegalArgumentException("Invalid value for id @pos #" + index + ", val = " + val);
            } else {
                set.add(id);
            }
        }

        if (set.isEmpty() && !lenient)
            throw new IllegalArgumentException("No id to extract");

        return set;
    }
}
