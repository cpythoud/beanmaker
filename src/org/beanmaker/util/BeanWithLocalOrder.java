package org.beanmaker.util;

public interface BeanWithLocalOrder {

    long getId();

    String getLocalOrderTable();

    long getItemOrder(final TableLocalOrderContext context);
}
