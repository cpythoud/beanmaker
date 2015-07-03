package org.beanmaker.util;

public interface DbBeanWithItemOrderInterface<T> extends DbBeanInterface {

    boolean isFirstItemOrder();
    boolean isLastItemOrder();

    void itemOrderMoveUp();
    void itemOrderMoveDown();

    void itemOrderMoveAfter(T bean);
    void itemOrderMoveBefore(T bean);
}
