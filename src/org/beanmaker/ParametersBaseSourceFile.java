package org.beanmaker;

import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ReturnStatement;

import org.dbbeans.util.Strings;

public class ParametersBaseSourceFile extends BeanCodeWithDBInfo {

    public ParametersBaseSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "ParametersBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.dbbeans.util.Strings");

        importsManager.addImport("java.util.Arrays");
        importsManager.addImport("java.util.List");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract();
    }

    private void addNamingFieldsGetter() {
        addStringListGetter("getNamingFields");
    }

    private void addOrderingFieldsGetter() {
        addStringListGetter("getOrderingFields");
    }

    private void addStringListGetter(final String getter) {
        javaClass.addContent(
                new FunctionDeclaration(getter, "List<String>").addContent(
                        new ReturnStatement(
                                new FunctionCall("asList", "Arrays").addArgument(Strings.quickQuote(columns.getOrderByField()))
                        )
                )
        ).addContent(EMPTY_LINE);
    }

    private void addOrderByFieldsGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getOrderByFields", "String").addContent(
                        new ReturnStatement(
                                new FunctionCall("concatWithSeparator", "Strings")
                                        .addArgument(Strings.quickQuote(", "))
                                        .addArgument(new FunctionCall("getOrderingFields"))
                        )
                )
        );
    }

    private void addItemOrderExtraSqlConditionGetter() {
        newLine();
        javaClass.addContent(
                new FunctionDeclaration("getItemOrderExtraSqlCondition", "String").addContent(
                        new ReturnStatement("null")
                )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addNamingFieldsGetter();
        addOrderingFieldsGetter();
        addOrderByFieldsGetter();
        if (columns.hasItemOrder())
            addItemOrderExtraSqlConditionGetter();
    }
}
