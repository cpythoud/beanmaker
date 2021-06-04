package org.beanmaker.util;

import org.apache.commons.fileupload.FileItem;

import java.io.File;

public class DbBeanFileCreator {

    public static final String SUBDIR_PREFIX = "P-";

    private final String defaultUploadDir;
    private final String alternateUploadDir;
    private final DbBeanFileStoredFilenameCalculator storedFilenameCalculator;
    private final int newUploadSubDirFileCountThreshold;

    /**
     * @deprecated Please do not use directly and/or regenerate your BeanHTMLViewBase class to get rid of the deprecation warning
     */
    @Deprecated
    public DbBeanFileCreator(final String defaultUploadDir) {
        this(defaultUploadDir, new DbBeanFileIdentityStoredFilenameCalculator());
    }

    public DbBeanFileCreator(final String defaultUploadDir, int newUploadSubDirFileCountThreshold) {
        this(defaultUploadDir, new DbBeanFileIdentityStoredFilenameCalculator(), newUploadSubDirFileCountThreshold);
    }

    /**
     * @deprecated Please do not use directly and/or regenerate your BeanHTMLViewBase class to get rid of the deprecation warning
     */
    @Deprecated
    public DbBeanFileCreator(
            final String defaultUploadDir,
            final DbBeanFileStoredFilenameCalculator storedFilenameCalculator)
    {
        this(defaultUploadDir, null, storedFilenameCalculator);
    }

    public DbBeanFileCreator(
            final String defaultUploadDir,
            final DbBeanFileStoredFilenameCalculator storedFilenameCalculator,
            final int newUploadSubDirFileCountThreshold)
    {
        this(defaultUploadDir, null, storedFilenameCalculator, newUploadSubDirFileCountThreshold);
    }

    /**
     * @deprecated Please do not use directly and/or regenerate your BeanHTMLViewBase class to get rid of the deprecation warning
     */
    @Deprecated
    public DbBeanFileCreator(
            final String defaultUploadDir,
            final String alternateUploadDir,
            final DbBeanFileStoredFilenameCalculator storedFilenameCalculator)
    {
        this(defaultUploadDir, alternateUploadDir, storedFilenameCalculator, 0);
    }

    public DbBeanFileCreator(
            final String defaultUploadDir,
            final String alternateUploadDir,
            final DbBeanFileStoredFilenameCalculator storedFilenameCalculator,
            final int newUploadSubDirFileCountThreshold)
    {
        this.defaultUploadDir = defaultUploadDir;
        if (alternateUploadDir == null || alternateUploadDir.equals(defaultUploadDir))
            this.alternateUploadDir = null;
        else
            this.alternateUploadDir = alternateUploadDir;
        this.storedFilenameCalculator = storedFilenameCalculator;
        this.newUploadSubDirFileCountThreshold = newUploadSubDirFileCountThreshold;
    }

    public DbBeanFile create(final DbBeanFile dbBeanFile, final FileItem fileItem) {
        boolean newRecord = false;

        dbBeanFile.setOrigFilename(fileItem.getName());
        final String filename = storedFilenameCalculator.calc(fileItem.getName());
        if (filename.equals(dbBeanFile.getOrigFilename()))
            dbBeanFile.setStoredFilename(null);
        else
            dbBeanFile.setStoredFilename(filename);

        if (alternateUploadDir != null)
            dbBeanFile.setAltDir(alternateUploadDir);

        if (dbBeanFile.getId() == 0) {
            dbBeanFile.updateDB();
            newRecord = true;
        }

        try {
            fileItem.write(new File(getOrCreateUploadDirectory(dbBeanFile), filename));
        } catch (final Exception ex) {  // function write() is actually marked as throwing Exception !!!
            if (newRecord)
                dbBeanFile.delete();
            throw new RuntimeException(ex);
        }

        if (!newRecord)
            dbBeanFile.updateDB();

        return dbBeanFile;
    }

    private File getOrCreateUploadDirectory(final DbBeanFile dbBeanFile) {
        final File uploadDirectory = getUploadDirectory(dbBeanFile, defaultUploadDir, newUploadSubDirFileCountThreshold);

        if (!uploadDirectory.exists()) {
            if (!uploadDirectory.mkdirs())
                throw new IllegalArgumentException("Could not create directory: " + uploadDirectory.getAbsolutePath());
        } else if (!uploadDirectory.isDirectory())
            throw new IllegalArgumentException(uploadDirectory.getAbsolutePath() + " is not a directory");

        return uploadDirectory;
    }

    /**
     * @deprecated Please use File getUploadDirectory(DbBeanFile file, String defaultUploadDir, int newUploadSubDirFileCountThreshold) instead
     */
    @Deprecated
    public static File getUploadDirectory(final DbBeanFile dbBeanFile, final String defaultUploadDir) {
        if (dbBeanFile.isAltDirEmpty())
            return new File(defaultUploadDir, Long.toString(dbBeanFile.getId()));

        return new File(dbBeanFile.getAltDir(), Long.toString(dbBeanFile.getId()));
    }

    public static File getUploadDirectory(final DbBeanFile dbBeanFile, final String defaultUploadDir, final int newUploadSubDirFileCountThreshold) {
        if (dbBeanFile.isAltDirEmpty()) {
            final File baseDir;
            if (newUploadSubDirFileCountThreshold == 0)
                baseDir = new File(defaultUploadDir);
            else
                baseDir = new File(defaultUploadDir, SUBDIR_PREFIX + (dbBeanFile.getId() / newUploadSubDirFileCountThreshold));
            return new File(baseDir, Long.toString(dbBeanFile.getId()));
        }

        return new File(dbBeanFile.getAltDir(), Long.toString(dbBeanFile.getId()));
    }
}
