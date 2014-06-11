package org.beanmaker;

import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ReturnStatement;

import org.dbbeans.util.Strings;

public class ParametersBaseSourceFile extends BeanCodeWithDBInfo {

    private Column itemOrderField = null;

    public ParametersBaseSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "ParametersBase", columns, tableName);

        if (columns.hasItemOrder())
            itemOrderField = columns.getItemOrderField();

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

    private void addItemOrderMaxQueryGetter() {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT MAX(item_order) FROM ").append(tableName);
        if (!itemOrderField.isUnique())
            query.append(" WHERE ").append(itemOrderField.getItemOrderAssociatedField()).append("=?");

        newLine();
        javaClass.addContent(
                new FunctionDeclaration("getItemOrderMaxQuery", "String").addContent(
                        new ReturnStatement(Strings.quickQuote(query.toString()))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addItemOrderMaxQueryGetterWithNullSecondaryFieldGetter() {
        final String query =
                "SELECT MAX(item_order) FROM " + tableName + " WHERE " + itemOrderField.getItemOrderAssociatedField() + " IS NULL";

        javaClass.addContent(
                new FunctionDeclaration("getItemOrderMaxQueryWithNullSecondaryField", "String").addContent(
                        new ReturnStatement(Strings.quickQuote(query))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addIdFromItemOrderQueryGetter() {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT id FROM ").append(tableName).append(" WHERE item_order=?");
        if (!itemOrderField.isUnique())
            query.append(" AND ").append(itemOrderField.getItemOrderAssociatedField()).append("=?");

        javaClass.addContent(
                new FunctionDeclaration("getIdFromItemOrderQuery", "String").addContent(
                        new ReturnStatement(Strings.quickQuote(query.toString()))
                )
        );
    }

    private void addIdFromItemOrderQueryWithNullSecondaryFieldGetter() {
        final String query =
                "SELECT id FROM " + tableName + " WHERE item_order=? AND " + itemOrderField.getItemOrderAssociatedField() + " IS NULL";

        newLine();
        javaClass.addContent(
                new FunctionDeclaration("getIdFromItemOrderQueryWithNullSecondaryField", "String").addContent(
                        new ReturnStatement(Strings.quickQuote(query))
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
        if (columns.hasItemOrder()) {
            addItemOrderMaxQueryGetter();
            if (!itemOrderField.isUnique())
                addItemOrderMaxQueryGetterWithNullSecondaryFieldGetter();
            addIdFromItemOrderQueryGetter();
            if (!itemOrderField.isUnique())
                addIdFromItemOrderQueryWithNullSecondaryFieldGetter();
        }
    }
}
