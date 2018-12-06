package org.beanmaker;

import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class ParametersSourceFile extends BaseCode {

    private final String beanName;

    public ParametersSourceFile(final String beanName, final String packageName) {
        super(beanName + "Parameters", packageName);

        this.beanName = beanName;

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanCacheSet");
    }

    private void addCacheDeclarations() {
        javaClass.addContent(
                new VarDeclaration(
                        "DbBeanCacheSet<" + beanName + ">",
                        "CACHE_SET",
                        "null",
                        Visibility.PUBLIC).markAsStatic().markAsFinal()
        ).addContent(
                new VarDeclaration(
                        "boolean",
                        "USE_CACHE",
                        "CACHE_SET != null",
                        Visibility.PUBLIC).markAsStatic().markAsFinal()
        ).addContent(
                new VarDeclaration(
                        "boolean",
                        "PREVENT_CACHE_USE_WITH_TRANSACTIONS",
                        "true",
                        Visibility.PUBLIC).markAsStatic().markAsFinal()
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass(beanName + "ParametersBase");

        addImports();
        addCacheDeclarations();
    }
}
