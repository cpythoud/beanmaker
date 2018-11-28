package org.beanmaker;

import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;

public class InPackageExcelExportSourceFile extends BaseCode {

    public InPackageExcelExportSourceFile(final String packageName) {
        super("ExcelExport", packageName);

        javaClass.extendsClass("BaseExcelExport");
        javaClass.markAsAbstract();
        javaClass.implementsInterface("DbBeanExcelExport");

        createSourceCode();
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addConstructor();
        addSetLanguageFunction();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.BaseExcelExport");
        importsManager.addImport("org.beanmaker.util.DbBeanExcelExport");
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.MissingImplementationException");
    }

    private void addConstructor() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument("String", "resourceBundleName"))
                        .addArgument(new FunctionArgument("String", "tabName"))
                        .addContent(
                                new FunctionCall("super").addArguments("resourceBundleName", "tabName")
                                        .byItself()
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addSetLanguageFunction() {
        addNonImplementedFunction("setLanguage", new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"));
    }
}
