package org.beanmaker;

import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.Visibility;

public class InPackageBaseServletSourceFile extends BaseCode {

    public InPackageBaseServletSourceFile(String packageName) {
        super("BaseServlet", packageName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.MissingImplementationException");
        importsManager.addImport("org.beanmaker.util.OperationsBaseServlet");

        importsManager.addImport("javax.servlet.http.HttpSession");
    }

    private void addNonImplementedFunction() {
        addNonImplementedOverriddenFunction(
                "DbBeanLanguage",
                "getLanguage",
                Visibility.PROTECTED,
                new FunctionArgument("HttpSession", "session"));
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass("OperationsBaseServlet").markAsAbstract().addContent(EMPTY_LINE);

        addImports();
        addNonImplementedFunction();
    }

}
