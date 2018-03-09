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
        addNonImplementedFunction("DbBeanFile", "get", new FunctionArgument("long", "id"));

        addNonImplementedFunction(
                "DbBeanFile",
                "getOrCreate",
                new FunctionArgument("long", "id"));

        addNonImplementedFunction("boolean", "isIdOK", new FunctionArgument("long", "id"));

        addNonImplementedFunction(
                "boolean",
                "isIdOK",
                new FunctionArgument("long", "id"),
                new FunctionArgument("DBTransaction", "transaction"));

        addNonImplementedFunction(
                "String",
                "getFilename",
                new FunctionArgument("long", "id"));

        addNonImplementedFunction("String", "getDefaultUploadDir");

        addNonImplementedFunction(
                "DbBeanFileStoredFilenameCalculator",
                "getDefaultFileStoredFileNameCalculator");
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addNonImplementedFunctions();
    }
}
