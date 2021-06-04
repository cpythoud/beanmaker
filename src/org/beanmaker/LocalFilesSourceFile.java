package org.beanmaker;

import org.jcodegen.java.FunctionArgument;

public class LocalFilesSourceFile extends BaseCode {

    public LocalFilesSourceFile(final String packageName) {
        super("LocalFiles", packageName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanFile");
        importsManager.addImport("org.beanmaker.util.DbBeanFileStoredFilenameCalculator");
        importsManager.addImport("org.beanmaker.util.MissingImplementationException");

        importsManager.addImport("org.dbbeans.sql.DBTransaction");
    }

    private void addNonImplementedFunctions() {
        addNonImplementedStaticFunction("DbBeanFile", "get", new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction(
                "DbBeanFile",
                "getOrCreate",
                new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction("boolean", "isIdOK", new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction(
                "boolean",
                "isIdOK",
                new FunctionArgument("long", "id"),
                new FunctionArgument("DBTransaction", "transaction"));

        addNonImplementedStaticFunction(
                "String",
                "getFilename",
                new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction("String", "getDefaultUploadDir");

        addNonImplementedStaticFunction(
                "DbBeanFileStoredFilenameCalculator",
                "getDefaultFileStoredFileNameCalculator");

        addNonImplementedStaticFunction(
                "int",
                "getSubDirFileCountThreshold");
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addNonImplementedFunctions();
    }
}
