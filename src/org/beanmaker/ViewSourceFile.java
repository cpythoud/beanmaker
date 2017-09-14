package org.beanmaker;

import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;

public class ViewSourceFile extends BeanCode {

    private final boolean withLanguage;

    public ViewSourceFile(final String beanName, final String packageName, final String viewName) {
        this(beanName, packageName, viewName, false);
    }

    public ViewSourceFile(final String beanName, final String packageName, final String viewName, final boolean withLanguage) {
        super(beanName, packageName, viewName + "View");
        this.withLanguage = withLanguage;
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

    private void addConstructorWithBeanAndLanguageArguments() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new FunctionCall("super")
                                        .addArguments(beanVarName, "dbBeanLanguage")
                                        .byItself()
                        )
        );

    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addNoArgumentConstructor();
        newLine();
        addConstructorWithBeanArgument();

        if (withLanguage) {
            importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
            newLine();
            addConstructorWithBeanAndLanguageArguments();
        }
    }
}
