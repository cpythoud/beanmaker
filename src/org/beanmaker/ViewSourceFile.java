package org.beanmaker;

import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;

public class ViewSourceFile extends BeanCode {

    public ViewSourceFile(final String beanName, final String packageName, final String viewName) {
        super(beanName, packageName, viewName + "View");
        javaClass.extendsClass(beanName + viewName + "ViewBase");
        createSourceCode();
    }

    private void addNoArgumentConstructor() {
        javaClass.addContent(
                javaClass.createConstructor()
        );
    }

    private void addConstructorWithBeanArgument() {
        javaClass.addContent(
                javaClass.createConstructor().addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                        new FunctionCall("super").addArgument(beanVarName).byItself()
                )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addNoArgumentConstructor();
        newLine();
        addConstructorWithBeanArgument();
    }
}
