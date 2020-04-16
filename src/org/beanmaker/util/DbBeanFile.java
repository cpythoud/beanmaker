package org.beanmaker.util;

import java.io.File;

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
    String getInternalFilename();
    String getFileUrl();

    void makeSureFileExists();
}
