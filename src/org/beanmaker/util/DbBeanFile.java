package org.beanmaker.util;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;

public interface DbBeanFile extends DbBeanInterface {

    String getCode();
    String getOrigFilename();
    String getStoredFilename();
    String getAltDir();

    void setCode(final String code);
    void setOrigFilename(final String origFilename);
    void setStoredFilename(final String storedFilename);
    void setAltDir(final String altDir);

    boolean isStoredFilenameEmpty();
    boolean isAltDirEmpty();

    File getFile();
    File getSafeNameFileCopy();
    String getInternalFilename();
    String getFileUrl();

    Date getModificationDate();
    Timestamp getModificationTimestamp();

    void makeSureFileExists();
}
