package org.beanmaker.util;

import java.util.Comparator;

public class MultilingualComparator<T extends MultilingualIdNamePairBean> implements Comparator<T> {

    private final DbBeanLanguage dbBeanLanguage;

    public MultilingualComparator(final DbBeanLanguage dbBeanLanguage) {
        this.dbBeanLanguage = dbBeanLanguage;
    }

    @Override
    public int compare(final T bean1, final T bean2) {
        return bean1.getNameForPair(dbBeanLanguage).compareTo(bean2.getNameForPair(dbBeanLanguage));
    }
}
