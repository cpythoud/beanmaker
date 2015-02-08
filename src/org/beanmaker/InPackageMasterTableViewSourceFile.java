package org.beanmaker;


import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;

public class InPackageMasterTableViewSourceFile extends BaseCode {

    public InPackageMasterTableViewSourceFile(final String packageName) {
        super("MasterTableView", packageName);
        javaClass.extendsClass("BaseMasterTableView");
        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.BaseMasterTableView");
    }

    public void addConstructor() {
        javaClass.addContent(
                javaClass.createConstructor().addArgument(new FunctionArgument("String", "resourceBundleName")).addArgument(new FunctionArgument("String", "tableId")).addContent(
                        new FunctionCall("super").addArguments("resourceBundleName", "tableId").byItself()
                )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        javaClass.markAsAbstract();
        addImports();
        addConstructor();
    }
}
