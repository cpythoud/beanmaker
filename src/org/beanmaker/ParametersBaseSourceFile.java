package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

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
        addStringListGetter("getNamingFields", columns.getNamingField());
    }

    private void addOrderingFieldsGetter() {
        if (itemOrderField == null || itemOrderField.isUnique())
            addStringListGetter("getOrderingFields", columns.getOrderByField());
        else
            addStringListGetter(
                    "getOrderingFields",
                    itemOrderField.getItemOrderAssociatedField(),
                    columns.getOrderByField());
    }

    private void addStringListGetter(final String getter, final String... originalValues) {
        FunctionCall arrayFunctionCall = new FunctionCall("asList", "Arrays");
        for (String originalValue: originalValues)
            arrayFunctionCall.addArgument(Strings.quickQuote(originalValue));

        javaClass.addContent(
                new FunctionDeclaration(getter, "List<String>")
                        .addContent(new ReturnStatement(arrayFunctionCall))
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

    private StringBuilder getItemOrderMaxBaseQuery() {
        return new StringBuilder().append("SELECT MAX(item_order) FROM ").append(tableName);
    }

    private void addItemOrderMaxQueryGetter() {
        final StringBuilder query = getItemOrderMaxBaseQuery();
        if (!itemOrderField.isUnique())
            appendSecondaryFieldCondition(query, false, true);

        newLine();
        addFunctionReturningQueryAsString("getItemOrderMaxQuery", query);
    }

    private void addItemOrderMaxQueryGetterWithNullSecondaryFieldGetter() {
        final StringBuilder query = getItemOrderMaxBaseQuery();
        appendSecondaryFieldCondition(query, true, true);

        addFunctionReturningQueryAsString("getItemOrderMaxQueryWithNullSecondaryField", query);
    }

    private StringBuilder getIdFromItemOrderBaseQuery() {
        return new StringBuilder().append("SELECT id FROM ").append(tableName).append(" WHERE item_order=?");
    }

    private void addIdFromItemOrderQueryGetter() {
        final StringBuilder query = getIdFromItemOrderBaseQuery();
        if (!itemOrderField.isUnique())
            appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getIdFromItemOrderQuery", query);
    }

    private void addIdFromItemOrderQueryWithNullSecondaryFieldGetter() {
        final StringBuilder query = getIdFromItemOrderBaseQuery();
        appendSecondaryFieldCondition(query, true, false);

        addFunctionReturningQueryAsString("getIdFromItemOrderQueryWithNullSecondaryField", query);
    }

    private StringBuilder getUpdateItemOrdersAboveBaseQuery() {
        return new StringBuilder().append("UPDATE ").append(tableName).append(" SET item_order=item_order-1 WHERE item_order > ?");
    }

    private void addUpdateItemOrdersAboveQueryGetter() {
        final StringBuilder query = getUpdateItemOrdersAboveBaseQuery();
        if (!itemOrderField.isUnique())
            appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getUpdateItemOrdersAboveQuery", query);
    }

    private void addUpdateItemOrdersAboveQueryWithNullSecondaryField() {
        final StringBuilder query = getUpdateItemOrdersAboveBaseQuery();
        appendSecondaryFieldCondition(query, true, false);

        addFunctionReturningQueryAsString("getUpdateItemOrdersAboveQueryWithNullSecondaryField", query);
    }

    private StringBuilder getDecreaseItemOrderBetweenBaseQuery() {
        return new StringBuilder().append("UPDATE ").append(tableName).append(" SET item_order=item_order-1 WHERE item_order > ? AND item_order < ?");
    }

    private void addDecreaseItemOrderBetweenQueryGetter() {
        final StringBuilder query = getDecreaseItemOrderBetweenBaseQuery();
        if (!itemOrderField.isUnique())
            appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getDecreaseItemOrderBetweenQuery", query);
    }

    private void addDecreaseItemOrderBetweenQueryGetterWithNullSecondaryField() {
        final StringBuilder query = getDecreaseItemOrderBetweenBaseQuery();
        appendSecondaryFieldCondition(query, true, false);

        addFunctionReturningQueryAsString("getDecreaseItemOrderBetweenQueryWithNullSecondaryField", query);
    }

    private StringBuilder getIncreaseItemOrderBetweenBaseQuery() {
        return new StringBuilder().append("UPDATE ").append(tableName).append(" SET item_order=item_order+1 WHERE item_order > ? AND item_order < ?");
    }

    private void addIncreaseItemOrderBetweenQueryGetter() {
        final StringBuilder query = getIncreaseItemOrderBetweenBaseQuery();
        if (!itemOrderField.isUnique())
            appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getIncreaseItemOrderBetweenQuery", query);
    }

    private void addIncreaseItemOrderBetweenQueryGetterWithNullSecondaryField() {
        final StringBuilder query = getIncreaseItemOrderBetweenBaseQuery();
        appendSecondaryFieldCondition(query,  true, false);

        addFunctionReturningQueryAsString("getIncreaseItemOrderBetweenQueryWithNullSecondaryField", query);
    }

    private StringBuilder getPushItemOrdersUpBaseQuery() {
        return new StringBuilder().append("UPDATE ").append(tableName).append(" SET item_order=item_order+1 WHERE item_order > ?");
    }

    private void addPushItemOrdersUpQueryGetter() {
        final StringBuilder query = getPushItemOrdersUpBaseQuery();
        appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getPushItemOrdersUpQuery", query);
    }

    private void addPushItemOrdersUpQueryGetterWithNullSecondaryField() {
        final StringBuilder query = getPushItemOrdersUpBaseQuery();
        appendSecondaryFieldCondition(query, true, false);

        addFunctionReturningQueryAsString("getPushItemOrdersUpQueryWithNullSecondaryField", query);
    }

    private StringBuilder getPushItemOrdersDownBaseQuery() {
        return new StringBuilder().append("UPDATE ").append(tableName).append(" SET item_order=item_order-1 WHERE item_order > ?");
    }

    private void addPushItemOrdersDownQueryGetter() {
        final StringBuilder query = getPushItemOrdersDownBaseQuery();
        appendSecondaryFieldCondition(query, false, false);

        addFunctionReturningQueryAsString("getPushItemOrdersDownQuery", query);
    }

    private void addPushItemOrdersDownQueryGetterWithNullSecondaryField() {
        final StringBuilder query = getPushItemOrdersDownBaseQuery();
        appendSecondaryFieldCondition(query, true, false);

        addFunctionReturningQueryAsString("getPushItemOrdersDownQueryWithNullSecondaryField", query);
    }

    private void addDefaultMoneyFormatGetter() {
        importsManager.addImport("org.dbbeans.util.MoneyFormat");
        newLine();
        javaClass.addContent(
                new FunctionDeclaration("getDefaultMoneyFormat", "MoneyFormat").addContent(
                        new ReturnStatement(
                                new FunctionCall("getDefault", "MoneyFormat")
                        )
                )
        );
    }

    private void addDefaultRequiredLanguagesForLabels() {
        importsManager.addImport("org.beanmaker.util.DbBeanRequiredLanguages");
        newLine();
        for (Column column: columns.getList())
            if (column.isLabelReference()) {
                final ObjectCreation requiredLanguageObjectCreation = new ObjectCreation("DbBeanRequiredLanguages");
                if (column.isRequired())
                    requiredLanguageObjectCreation.addArgument(
                            new FunctionCall("getAllActiveLanguages", "Labels")
                    );

                javaClass.addContent(
                        new VarDeclaration(
                                "DbBeanRequiredLanguages",
                                "requiredLanguagesFor" + SourceFiles.chopId(column.getJavaName()),
                                requiredLanguageObjectCreation,
                                Visibility.PROTECTED)
                                .markAsFinal()
                );
            }
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
            addUpdateItemOrdersAboveQueryGetter();
            if (!itemOrderField.isUnique())
                addUpdateItemOrdersAboveQueryWithNullSecondaryField();
            addDecreaseItemOrderBetweenQueryGetter();
            if (!itemOrderField.isUnique())
                addDecreaseItemOrderBetweenQueryGetterWithNullSecondaryField();
            addIncreaseItemOrderBetweenQueryGetter();
            if (!itemOrderField.isUnique()) {
                addIncreaseItemOrderBetweenQueryGetterWithNullSecondaryField();
                addPushItemOrdersUpQueryGetter();
                addPushItemOrdersUpQueryGetterWithNullSecondaryField();
                addPushItemOrdersDownQueryGetter();
                addPushItemOrdersDownQueryGetterWithNullSecondaryField();
            }
        }
        if (columns.containsFinancialData())
            addDefaultMoneyFormatGetter();
        if (columns.hasLabels())
            addDefaultRequiredLanguagesForLabels();
    }

    private void appendSecondaryFieldCondition(final StringBuilder query, final boolean isNull, final boolean firstCondition) {
        if (firstCondition)
            query.append(" WHERE ");
        else
            query.append(" AND ");
        query.append(itemOrderField.getItemOrderAssociatedField());
        if (isNull)
            query.append(" IS NULL");
        else
            query.append("=?");
    }

    private void addFunctionReturningQueryAsString(final String functionName, final StringBuilder query) {
        javaClass.addContent(
                new FunctionDeclaration(functionName, "String").addContent(
                        new ReturnStatement(Strings.quickQuote(query.toString()))
                )
        ).addContent(EMPTY_LINE);
    }
}
