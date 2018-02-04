package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

public interface TableLocalOrderContext {

    long getId();

    String getCode();

    DBTransaction getDBTransaction();
}
