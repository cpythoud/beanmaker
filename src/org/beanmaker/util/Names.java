package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Names {

    public static <B extends NamedDbBean> List<String> getList(final List<B> beans) {
        final List<String> names = new ArrayList<String>();

        for (B bean: beans)
            names.add(bean.getDisplayName());

        return names;
    }

    public static <B extends NamedDbBean> List<String> getSortedList(final List<B> beans) {
        final List<String> names = getList(beans);
        Collections.sort(names);
        return names;
    }

    public static <B extends MultiLingualNamedDbBean> List<String> getList(
            final List<B> beans,
            final DbBeanLanguage dbBeanLanguage)
    {
        final List<String> names = new ArrayList<String>();

        for (B bean: beans)
            names.add(bean.getDisplayName(dbBeanLanguage));

        return names;
    }

    public static <B extends MultiLingualNamedDbBean> List<String> getSortedList(
            final List<B> beans,
            final DbBeanLanguage dbBeanLanguage)
    {
        final List<String> names = getList(beans, dbBeanLanguage);
        Collections.sort(names);
        return names;
    }

    public static <B extends DbBeanMultilingual> List<String> getListFromMultilingualBeans(
            final List<B> beans,
            final DbBeanLanguage dbBeanLanguage)
    {
        final List<String> names = new ArrayList<String>();

        for (B bean: beans)
            names.add(bean.getLabel(dbBeanLanguage));

        return names;
    }

    public static <B extends DbBeanMultilingual> List<String> getSortedListFromMultilingualBeans(
            final List<B> beans,
            final DbBeanLanguage dbBeanLanguage)
    {
        final List<String> names = getListFromMultilingualBeans(beans, dbBeanLanguage);
        Collections.sort(names);
        return names;
    }
}
