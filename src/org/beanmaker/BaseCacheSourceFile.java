package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.ConstructorDeclaration;
import org.jcodegen.java.ForEach;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.Visibility;

public class BaseCacheSourceFile extends BeanCode {

    public BaseCacheSourceFile(final String beanName, final String packageName) {
        super(beanName, packageName, "CacheBase");

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanCache");
    }

    private void addConstructors() {
        final String className = beanName + "CacheBase";

        javaClass.addContent(
                new ConstructorDeclaration(className).addContent(
                        new FunctionCall("this")
                                .addArgument(Strings.quickQuote("all"))
                                .byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new ConstructorDeclaration(className)
                        .addArgument(new FunctionArgument("String", "code"))
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new FunctionCall("super")
                                        .addArgument("code")
                                        .byItself()
                        ).addContent(EMPTY_LINE).addContent(
                        new FunctionCall("defaultInit").byItself()
                )
        ).addContent(EMPTY_LINE);
    }

    private void addDefaultInitFunction() {
        javaClass.addContent(
                new FunctionDeclaration("defaultInit")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ForEach(beanName, beanVarName, new FunctionCall("getAll", beanName)).addContent(
                                        new FunctionCall("submit")
                                                .addArgument(beanVarName)
                                                .byItself()
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass("DbBeanCache<" + beanName + ">");

        addImports();
        addConstructors();
        addDefaultInitFunction();
    }
}
