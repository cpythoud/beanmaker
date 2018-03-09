package org.beanmaker.util;

import org.apache.commons.fileupload.FileItem;

import java.io.File;

public class DbBeanFileCreator {

    private final String defaultUploadDir;
    private final DbBeanFileStoredFilenameCalculator storedFilenameCalculator;

    public DbBeanFileCreator(final String defaultUploadDir) {
        this(defaultUploadDir, new DbBeanFileIdentityStoredFilenameCalculator());
    }

    public DbBeanFileCreator(
            final String defaultUploadDir,
            final DbBeanFileStoredFilenameCalculator storedFilenameCalculator)
    {
        this.defaultUploadDir = defaultUploadDir;
        this.storedFilenameCalculator = storedFilenameCalculator;
    }

    public DbBeanFile create(final DbBeanFile dbBeanFile, final FileItem fileItem) {
        boolean newRecord = false;

        dbBeanFile.setOrigFilename(fileItem.getName());
        final String filename = storedFilenameCalculator.calc(fileItem.getName());
        if (filename.equals(dbBeanFile.getOrigFilename()))
            dbBeanFile.setStoredFilename(null);
        else
            dbBeanFile.setStoredFilename(filename);

        if (dbBeanFile.getId() == 0) {
            dbBeanFile.updateDB();
            newRecord = true;
        }

        try {
            fileItem.write(new File(getOrCreateUploadDirectory(dbBeanFile, defaultUploadDir), filename));
        } catch (final Exception ex) {  // function write() is actually marked as throwing Exception !!!
            if (newRecord)
                dbBeanFile.delete();
            throw new RuntimeException(ex);
        }

        if (!newRecord)
            dbBeanFile.updateDB();

        return dbBeanFile;
    }

    private File getOrCreateUploadDirectory(final DbBeanFile dbBeanFile, final String defaultUploadDir) {
        final File uploadDirectory = getUploadDirectory(dbBeanFile, defaultUploadDir);

        if (!uploadDirectory.exists()) {
            if (!uploadDirectory.mkdir())
                throw new IllegalArgumentException("Could not create directory: " + uploadDirectory.getAbsolutePath());
        } else if (!uploadDirectory.isDirectory())
            throw new IllegalArgumentException(uploadDirectory.getAbsolutePath() + " is not a directory");

        return uploadDirectory;
    }

    public static File getUploadDirectory(final DbBeanFile dbBeanFile, final String defaultUploadDir) {
        if (dbBeanFile.isAltDirEmpty())
            return new File(defaultUploadDir, Long.toString(dbBeanFile.getId()));

        return new File(dbBeanFile.getAltDir(), Long.toString(dbBeanFile.getId()));
    }
}
