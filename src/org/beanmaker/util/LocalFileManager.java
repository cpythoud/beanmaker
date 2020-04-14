package org.beanmaker.util;

import org.dbbeans.sql.DBTransaction;

public interface LocalFileManager {

    DbBeanFile get(final long id);

    DbBeanFile getOrCreate(final long id);

    boolean isIdOK(final long id);

    boolean isIdOK(final long id, final DBTransaction transaction);

    String getFilename(final long id);

    String getDefaultUploadDir();

    DbBeanFileStoredFilenameCalculator getDefaultFileStoredFileNameCalculator();

}
