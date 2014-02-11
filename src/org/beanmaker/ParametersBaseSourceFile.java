package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.StaticBlock;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import org.dbbeans.util.Strings;

public class ParametersBaseSourceFile extends BeanCodeWithDBInfo {

    public ParametersBaseSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "ParametersBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.ParametersBase");
        importsManager.addImport("org.dbbeans.util.Strings");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("ParametersBase");
    }

    private void addProperties() {
        javaClass.addContent(
                new VarDeclaration("String", "ORDER_BY_CLAUSE").markAsStatic().markAsFinal().visibility(Visibility.PRIVATE)
        ).addContent(EMPTY_LINE);
    }

    private void addStaticInitialization() {
        javaClass.addContent(
                new StaticBlock().addContent(
                        getAddingCall("NAMING_FIELDS")
                ).addContent(
                        getAddingCall("ORDERING_FIELDS")
                ).addContent(
                        new Assignment("ORDER_BY_CLAUSE", new FunctionCall("concatWithSeparator", "Strings").addArguments(Strings.quickQuote(", "),
                                "ORDERING_FIELDS"))
                )
        ).addContent(EMPTY_LINE);
    }

    private FunctionCall getAddingCall(final String fieldList) {
        return new FunctionCall("add", fieldList).addArgument(Strings.quickQuote(columns.getOrderByField())).byItself();
    }

    private void addOrderByFieldsGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getOrderByFields", "String").annotate("@Override").addContent(
                        new ReturnStatement("ORDER_BY_CLAUSE")
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
        addProperties();
        addStaticInitialization();
        addOrderByFieldsGetter();
        if (columns.hasItemOrder())
            addItemOrderExtraSqlConditionGetter();
    }
}
