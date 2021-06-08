package org.beanmaker.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DbBeanFileFilenameFixes {

    public static void replaceExtraDotsInInternalFilename(DbBeanFile dbBeanFile) throws IOException {
        replaceExtraDotsInInternalFilename(dbBeanFile, "_");
    }

    public static void replaceExtraDotsInInternalFilename(DbBeanFile dbBeanFile, String replacement) throws IOException {
        String[] parts = dbBeanFile.getOrigFilename().split("\\.");
        int length = parts.length;
        if (length <= 2)
            return;

        StringBuilder newFilename = new StringBuilder();
        newFilename.append(parts[0]);
        for (int i = 1; i < length - 1; ++i)
            newFilename.append(replacement).append(parts[i]);
        newFilename.append(".").append(parts[length - 1]);

        safeInternalRename(dbBeanFile, newFilename.toString());
    }

    private static void safeInternalRename(DbBeanFile dbBeanFile, String newFilename) throws IOException {
        File oldFile = dbBeanFile.getFile();
        File newFile = new File(oldFile.getParentFile(), newFilename);
        FileUtils.copyFile(oldFile, newFile);

        dbBeanFile.setOrigFilename(newFilename);
        dbBeanFile.updateDB();

        FileUtils.deleteQuietly(oldFile);
    }

    public static void replaceCharactersInInternalFilename(DbBeanFile dbBeanFile, String regex, String replacement) throws IOException {
        String[] parts = dbBeanFile.getOrigFilename().split(regex);
        int length = parts.length;
        if (length == 1)
            return;

        StringBuilder newFilename = new StringBuilder();
        newFilename.append(parts[0]);
        for (int i = 1; i < length; ++i)
            newFilename.append(replacement).append(parts[i]);

        safeInternalRename(dbBeanFile, newFilename.toString());
    }
}
