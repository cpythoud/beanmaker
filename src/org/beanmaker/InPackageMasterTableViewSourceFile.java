package org.beanmaker;


import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;

public class InPackageMasterTableViewSourceFile extends BaseCode {

    public InPackageMasterTableViewSourceFile(final String packageName) {
        super("MasterTableView", packageName);
        javaClass.extendsClass("BaseMasterTableView");
        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.BaseMasterTableView");
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
    }

    private void addConstructor() {
        javaClass.addContent(
                javaClass.createConstructor().addArgument(new FunctionArgument("String", "resourceBundleName")).addArgument(new FunctionArgument("String", "tableId")).addContent(
                        new FunctionCall("super").addArguments("resourceBundleName", "tableId").byItself()
                )
        );
    }

    private void addSetLanguageFunction() {
        javaClass.addContent(
                new FunctionDeclaration("setLanguage")
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new FunctionCall("setLanguage")
                                        .byItself()
                                        .addArgument(new FunctionCall("getCopy", "Labels")
                                                .addArgument("dbBeanLanguage"))
                                        .addArgument(new FunctionCall("getLabelMap", "Labels")
                                                .addArguments(
                                                        "dbBeanLanguage",
                                                        "\"cct_remove_filtering\", \"yes\", \"no\", \"cct_no_data\", \"cct_total\", \"cct_shown\", \"cct_filtered\""))
                        )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        javaClass.markAsAbstract();
        addImports();
        addConstructor();
        addSetLanguageFunction();
    }
}
