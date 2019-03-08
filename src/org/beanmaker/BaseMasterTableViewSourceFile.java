package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.ForEach;
import org.jcodegen.java.ForLoop;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.TernaryOperator;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.beanmaker.SourceFiles.chopId;

public class BaseMasterTableViewSourceFile extends BeanCodeForTabularView {

    private final String beanName;
    private final String beanVarName;
    //private final String bundleName;

    public BaseMasterTableViewSourceFile(
            final String beanName,
            final String packageName,
            final Columns columns,
            final String tableName)
    {
        super(beanName, "MasterTableView", packageName, "Base", columns, tableName);

        this.beanName = beanName;
        beanVarName = getBeanVarName(beanName);
        //bundleName = getBundleName(beanName,  packageName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.jcodegen.html.TdTag");
        importsManager.addImport("org.jcodegen.html.ThTag");
        importsManager.addImport("org.jcodegen.html.TrTag");

        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("java.util.List");

        if (columns.hasLabels())
            importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("MasterTableView");
    }

    private void addFilterRowFunctions() {
        final FunctionDeclaration masterFunction =
                getFilterOrTitleRowFunction("FilterRow", "filterRow");
        addFunctionCallsTo(masterFunction, "filterRow", "Filter");
        masterFunction
                .addContent(
                        new IfBlock(new Condition("doDataToggle"))
                                .addContent(
                                        new FunctionCall("child", "filterRow")
                                                .byItself()
                                                .addArgument(new FunctionCall("showMoreLessCell"))
                                )
                )
                .addContent(EMPTY_LINE)
                .addContent(new ReturnStatement("filterRow"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        addFilterCellGetterFunctions();
    }

    private FunctionDeclaration getFilterOrTitleRowFunction(final String endOfFunctionName, final String trName) {
        return new FunctionDeclaration("get" + endOfFunctionName, "TrTag")
                .annotate("@Override")
                .visibility(Visibility.PROTECTED)
                .addContent(
                        new VarDeclaration(
                                "TrTag",
                                trName,
                                new FunctionCall("getDefaultStartOf" + endOfFunctionName))
                                .markAsFinal())
                .addContent(EMPTY_LINE);
    }

    private FunctionDeclaration getTableLineFunction() {
        final FunctionCall getIdCall = new FunctionCall("getId", beanVarName);

        final Condition editLinkCheck;
        if (columns.hasItemOrder())
            editLinkCheck = new Condition("showEditLinks || showOrderingLinks || enableDragNDrop");
        else
            editLinkCheck = new Condition("showEditLinks");

        return new FunctionDeclaration("getTableLine", "TrTag")
                .addArgument(new FunctionArgument(beanName, beanVarName))
                .visibility(Visibility.PUBLIC)
                .addContent(
                        new VarDeclaration("TrTag", "line").markAsFinal()
                )
                .addContent(
                        new IfBlock(editLinkCheck)
                                .addContent(
                                        new Assignment(
                                                "line",
                                                new FunctionCall("getTrTag")
                                                        .addArgument(getIdCall))
                                ).elseClause(
                                new ElseBlock().addContent(
                                        new Assignment(
                                                "line",
                                                new FunctionCall("getTableLine")
                                                        .addArgument(getIdCall))
                                ))
                )
                .addContent(EMPTY_LINE);
    }

    private void addFunctionCallsTo(
            final FunctionDeclaration functionDeclaration,
            final String varName,
            final String cellType)
    {
        functionDeclaration.addContent(
                new IfBlock(new Condition("displayId")).addContent(getChildCall(varName, "id", cellType))
        );
        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.isLabelReference())
                    functionDeclaration.addContent(getAddLabelCalls(varName, column.getJavaName(), cellType));
                else
                    functionDeclaration.addContent(getChildCall(varName, column.getJavaName(), cellType));
            }

        functionDeclaration.addContent(EMPTY_LINE);
    }

    private FunctionCall getAddLabelCalls(final String varName, final String javaName, final String cellType) {
        return new FunctionCall("add" + chopId(javaName) + cellType + "CellsTo")
                .byItself()
                .addArgument(varName);
    }

    private FunctionCall getChildCall(final String varName, final String javaName, final String cellType) {
        return new FunctionCall("child", varName).byItself().addArgument(
                new FunctionCall("get" + Strings.capitalize(javaName) + cellType + "Cell")
        );
    }

    private void addFilterCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder()) {
                if (column.isLabelReference())
                    addFilterCellAddLabelFunctions(column);
                else
                    addFilterCellGetterFunction(column);
            }
    }

    private void addFilterCellAddLabelFunctions(final Column column) {
        final String choppedIdName = chopId(column.getJavaName());

        javaClass.addContent(getAddLabelLoopFunction("filterRow", choppedIdName, "Filter"))
                .addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("get" + choppedIdName + "FilterCell", "ThTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new ReturnStatement(new FunctionCall("getStringFilterCell")
                                        .addArgument("dbBeanLanguage.getIso() + " + Strings.quickQuote(choppedIdName))
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private FunctionDeclaration getAddLabelLoopFunction(
            final String varName,
            final String choppedIdName,
            final String cellType)
    {
        final FunctionCall labelChildFunctionCall = getLabelChildFunctionCall(varName, choppedIdName, cellType);

        return new FunctionDeclaration("add" + choppedIdName + cellType + "CellsTo")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("TrTag", varName))
                .addContent(
                        new IfBlock(new Condition("displayAllLanguages"))
                                .addContent(
                                        new ForLoop("DbBeanLanguage dbBeanLanguage: Labels.getAllActiveLanguages()")
                                                .addContent(labelChildFunctionCall)
                                )
                                .elseClause(new ElseBlock().addContent(labelChildFunctionCall))
                );
    }

    private FunctionCall getLabelChildFunctionCall(final String varName, final String choppedIdName, final String cellType) {
        return new FunctionCall("child", varName)
                .byItself()
                .addArgument(new FunctionCall("get" + choppedIdName + cellType + "Cell").addArgument("dbBeanLanguage"));
    }

    private void addFilterCellGetterFunction(final Column column) {
        final String filterFunctionName =
                column.getJavaType().equals("boolean") ? "getBooleanFilterCell" : "getStringFilterCell";
        javaClass.addContent(
                new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "FilterCell", "ThTag")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ReturnStatement(new FunctionCall(filterFunctionName)
                                        .addArgument(Strings.quickQuote(column.getJavaName())))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addTitleRowFunctions() {
        final FunctionDeclaration masterFunction =
                getFilterOrTitleRowFunction("TitleRow", "titleRow");
        addFunctionCallsTo(masterFunction, "titleRow", "Title");
        masterFunction.addContent(new ReturnStatement("titleRow"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        addTitleCellGetterFunctions();
    }

    private void addTitleCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder()) {
                if (column.isLabelReference())
                    addTitleCellAddLabelFunctions(column);
                else
                    addTitleCellGetterFunction(column);
            }
    }

    private void addTitleCellAddLabelFunctions(final Column column) {
        final String choppedIdName = chopId(column.getJavaName());

        javaClass.addContent(getAddLabelLoopFunction("titleRow", choppedIdName, "Title"))
                .addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("get" + choppedIdName + "TitleCell", "ThTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new ReturnStatement(new FunctionCall("getTitleCell")
                                        .addArgument("dbBeanLanguage.getIso() + " + Strings.quickQuote(choppedIdName))
                                        .addArgument("resourceBundle.getString("
                                                + Strings.quickQuote(column.getJavaName())
                                                + ") + \" \" + dbBeanLanguage.getCapIso()"))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addTitleCellGetterFunction(final Column column) {
        javaClass.addContent(
                new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "TitleCell", "ThTag")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getTitleCell")
                                                .addArgument(Strings.quickQuote(column.getJavaName()))
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addDataFunctions() {
        final String inventoryFunctionName = "get" + beanName + "Inventory";

        javaClass.addContent(
                new FunctionDeclaration("getData", "List<TrTag>")
                        .visibility(Visibility.PROTECTED)
                        .annotate("@Override")
                        .addContent(
                                VarDeclaration.createListDeclaration("TrTag", "lines")
                                        .markAsFinal()
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ForEach(beanName, beanVarName, new FunctionCall(inventoryFunctionName))
                                        .addContent(
                                                new FunctionCall("add", "lines")
                                                        .byItself()
                                                        .addArgument(new FunctionCall("getTableLine")
                                                                .addArgument(beanVarName))
                                )
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement("lines")
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration(inventoryFunctionName, "List<" + beanName + ">")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getBeansInLocalOrder")
                                                .addArgument(new FunctionCall("getAll", beanName)))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getLineCount", "long").visibility(Visibility.PROTECTED).annotate("@Override").addContent(
                        new ReturnStatement(
                                new FunctionCall("getCount", beanName)
                        )
                )
        ).addContent(EMPTY_LINE);


        final FunctionDeclaration masterFunction = getTableLineFunction();
        addFunctionCallsTo(masterFunction);
        masterFunction.addContent(new ReturnStatement("line"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        javaClass.addContent(getDataToLineFunction()).addContent(EMPTY_LINE);

        if (columns.hasItemOrder())
            addMultiOperationCellGetterFunction();
        else
            addOperationCellGetterFunction("edit");
        addTableCellGetterFunctions();
        addOkToDeleteFunction();
        addOperationCellGetterFunction("delete");
    }

    private void addFunctionCallsTo(final FunctionDeclaration functionDeclaration) {
        final Condition editLinkCheck;
        final String operationName;
        if (columns.hasItemOrder()) {
            editLinkCheck = new Condition("showEditLinks || showOrderingLinks || enableDragNDrop");
            operationName = "Operation";
        } else {
            editLinkCheck = new Condition("showEditLinks");
            operationName = "Edit";
        }

        functionDeclaration.addContent(
                new IfBlock(editLinkCheck)
                        .addContent(getOperationChildCall(operationName))
        );
        functionDeclaration.addContent(
                new IfBlock(new Condition("displayId")).addContent(getChildCall("id"))
        );
        functionDeclaration.addContent(
                new FunctionCall("addDataToLine")
                        .byItself()
                        .addArguments("line", beanVarName)
        );
        functionDeclaration.addContent(
                new IfBlock(new Condition("showEditLinks")
                        .andCondition(new Condition(new FunctionCall("okToDelete").addArgument(beanVarName))))
                        .addContent(getOperationChildCall("Delete"))
        );

        functionDeclaration.addContent(EMPTY_LINE);
    }

    private FunctionDeclaration getDataToLineFunction() {
        final FunctionDeclaration functionDeclaration = new FunctionDeclaration("addDataToLine")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("TrTag", "line"))
                .addArgument(new FunctionArgument(beanName, beanVarName));

        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.isLabelReference())
                    functionDeclaration.addContent(getAddLabelDataCall(column.getJavaName()));
                else
                    functionDeclaration.addContent(getChildCall(column.getJavaName()));
            }

        return functionDeclaration;
    }

    private FunctionCall getAddLabelDataCall(final String javaName) {
        return new FunctionCall("add" + chopId(javaName) + "DataCellsTo")
                .byItself()
                .addArguments("line", beanVarName);
    }

    private FunctionCall getChildCall(final String javaName) {
        return new FunctionCall("child", "line").byItself().addArgument(
                new FunctionCall("get" + Strings.capitalize(javaName) + "TableCell").addArgument(beanVarName)
        );
    }

    private FunctionCall getOperationChildCall(final String operationName) {
        return new FunctionCall("child", "line").byItself().addArgument(
                new FunctionCall("get" + operationName + "Cell").addArgument(beanVarName)
        );
    }

    private void addOperationCellGetterFunction(final String operation) {
        javaClass.addContent(
                new FunctionDeclaration("get" + Strings.capitalize(operation) + "Cell", "TdTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("get" + Strings.capitalize(operation) + "Cell")
                                                .addArgument(beanVarName)
                                                .addArgument(Strings.quickQuote(beanVarName))
                                                .addArgument(new FunctionCall("get", "Labels")
                                                        .addArgument(Strings.quickQuote(
                                                                "tooltip_" + operation + "_" + beanVarName))
                                                        .addArgument("dbBeanLanguage")))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addMultiOperationCellGetterFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getOperationCell", "TdTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getOperationCell")
                                                .addArgument(beanVarName)
                                                .addArgument(Strings.quickQuote(beanVarName))
                                                .addArguments(
                                                        new TernaryOperator(
                                                                new Condition("showEditLinks"),
                                                                new FunctionCall("get", "Labels")
                                                                        .addArgument(Strings.quickQuote(
                                                                                "tooltip_edit_" + beanVarName))
                                                                        .addArgument("dbBeanLanguage"),
                                                                "null")
                                                )
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addTableCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder()) {
                if (column.isLabelReference())
                    addTableCellAddLabelFunctions(column);
                else if (column.isFileReference())
                    addTableCellFileReferenceFunctions(column);
                else
                    addTableCellGetterFunction(column);
            }
    }

    private void addTableCellAddLabelFunctions(final Column column) {
        final String choppedIdName = chopId(column.getJavaName());
        final FunctionCall labelChildFunctionCall = getLabelChildFunctionCall(choppedIdName);

        javaClass.addContent(
                new FunctionDeclaration("add" + choppedIdName + "DataCellsTo")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("TrTag", "dataRow"))
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(
                                new IfBlock(new Condition("displayAllLanguages"))
                                        .addContent(
                                                new ForLoop("DbBeanLanguage dbBeanLanguage: Labels.getAllActiveLanguages()")
                                                        .addContent(labelChildFunctionCall)
                                        )
                                        .elseClause(new ElseBlock().addContent(labelChildFunctionCall))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("get" + choppedIdName + "TableCell", "TdTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(
                                new ReturnStatement(new FunctionCall("getTableCell")
                                        .addArgument("dbBeanLanguage.getIso() + " + Strings.quickQuote(choppedIdName))
                                        .addArgument(new FunctionCall("get", "Labels")
                                                .addArgument(new FunctionCall("getId" + choppedIdName, beanVarName))
                                                .addArgument("dbBeanLanguage")))
                        )
        );
    }

    private FunctionCall getLabelChildFunctionCall(final String choppedIdName) {
        return new FunctionCall("child", "dataRow")
                .byItself()
                .addArgument(new FunctionCall("get" + choppedIdName + "TableCell")
                        .addArgument("dbBeanLanguage")
                        .addArgument(beanVarName));
    }

    private void addTableCellFileReferenceFunctions(final Column column) {
        final String field = column.getJavaName();

        javaClass.addContent(
                new FunctionDeclaration("get" + Strings.capitalize(field) + "TableCell", "TdTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(new ReturnStatement(
                                new FunctionCall("getTableCell")
                                        .addArgument(Strings.quickQuote(field))
                                        .addArgument(getFilenameFunctionCall(beanVarName, field))
                        ))
        ).addContent(EMPTY_LINE);
    }

    private void addTableCellGetterFunction(final Column column) {
        final FunctionCall getTableCellCall =
                new FunctionCall("getTableCell")
                        .addArgument(Strings.quickQuote(column.getJavaName()));
        if (column.hasAssociatedBean())
            getTableCellCall.addArgument(
                    new FunctionCall("getHumanReadableTitle", column.getAssociatedBeanClass())
                            .addArgument(
                                    new FunctionCall("get" + Strings.capitalize(column.getJavaName()), beanVarName)
                            )
            );
        else {
            final String prefix = column.getJavaType().equals("boolean") ? "is" : "get";
            getTableCellCall.addArgument(new FunctionCall(prefix + Strings.capitalize(column.getJavaName()), beanVarName));
        }

        javaClass.addContent(
                new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "TableCell", "TdTag")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(new ReturnStatement(getTableCellCall))
        ).addContent(EMPTY_LINE);
    }

    private void addOkToDeleteFunction() {
        javaClass.addContent(
                new FunctionDeclaration("okToDelete", "boolean")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(new ReturnStatement("true"))
        ).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addConstructor();
        addFilterRowFunctions();
        addTitleRowFunctions();
        addDataFunctions();
    }
}
