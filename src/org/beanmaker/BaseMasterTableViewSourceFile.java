package org.beanmaker;

import org.dbbeans.util.Strings;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ForEach;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class BaseMasterTableViewSourceFile extends BeanCodeWithDBInfo {

    private final String beanName;
    private final String beanVarName;
    private final String bundleName;

    public BaseMasterTableViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName + "MasterTableView", packageName, "Base", columns, tableName);

        this.beanName = beanName;
        beanVarName = getBeanVarName(beanName);
        bundleName = getBundleName(beanName,  packageName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.jcodegen.html.TdTag");
        importsManager.addImport("org.jcodegen.html.ThTag");
        importsManager.addImport("org.jcodegen.html.TrTag");

        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("java.util.List");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("MasterTableView");
    }

    private void addConstructor() {
        javaClass.addContent(
                javaClass.createConstructor().addContent(
                        new FunctionCall("super").byItself().addArguments(Strings.quickQuote(bundleName), beanName + ".DATABASE_TABLE_NAME")
                )
        ).addContent(EMPTY_LINE);
    }

    private void addFilterRowFunctions() {
        final FunctionDeclaration masterFunction = getMasterFunction("FilterRow", "filterRow", true);
        addFunctionCallsTo(masterFunction, "filterRow", "Filter");
        masterFunction.addContent(new ReturnStatement("filterRow"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        addFilterCellGetterFunctions();
    }

    private FunctionDeclaration getMasterFunction(final String endOfFunctionName, final String trName, final boolean overrides) {
        final FunctionDeclaration functionDeclaration = new FunctionDeclaration("get" + endOfFunctionName, "TrTag").visibility(Visibility.PROTECTED);

        final FunctionCall initCall;
        if (overrides) {
            functionDeclaration.annotate("@Override");
            initCall = new FunctionCall("getDefaultStartOf" + endOfFunctionName);
        } else {
            functionDeclaration.addArgument(new FunctionArgument(beanName, beanVarName));
            initCall = new FunctionCall("get" + endOfFunctionName);
        }

        return functionDeclaration.addContent(new VarDeclaration("TrTag", trName, initCall).markAsFinal()).addContent(EMPTY_LINE);
    }

    private void addFunctionCallsTo(final FunctionDeclaration functionDeclaration, final String varName, final String cellType) {
        functionDeclaration.addContent(
                new IfBlock(new Condition("displayId")).addContent(getChildCall(varName, "id", cellType))
        );
        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder()))
                functionDeclaration.addContent(getChildCall(varName, column.getJavaName(), cellType));

        functionDeclaration.addContent(EMPTY_LINE);
    }

    private FunctionCall getChildCall(final String varName, final String javaName, final String cellType) {
        return new FunctionCall("child", varName).byItself().addArgument(
                new FunctionCall("get" + Strings.capitalize(javaName) + cellType + "Cell")
        );
    }

    private void addFilterCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder()) {
                final String filterFunctionName = column.getJavaType().equals("boolean") ? "getBooleanFilterCell" : "getStringFilterCell";
                javaClass.addContent(
                        new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "FilterCell", "ThTag").visibility(Visibility.PROTECTED).addContent(
                                new ReturnStatement(new FunctionCall(filterFunctionName).addArgument(Strings.quickQuote(column.getJavaName())))
                        )
                ).addContent(EMPTY_LINE);
            }
    }

    private void addTitleRowFunctions() {
        final FunctionDeclaration masterFunction = getMasterFunction("TitleRow", "titleRow", true);
        addFunctionCallsTo(masterFunction, "titleRow", "Title");
        masterFunction.addContent(new ReturnStatement("titleRow"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        addTitleCellGetterFunctions();
    }

    private void addTitleCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder())
                javaClass.addContent(
                        new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "TitleCell", "ThTag").visibility(Visibility.PROTECTED).addContent(
                                new ReturnStatement(new FunctionCall("getTitleCell").addArgument(Strings.quickQuote(column.getJavaName())))
                        )
                ).addContent(EMPTY_LINE);
    }

    private void addDataFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("getData", "List<TrTag>").visibility(Visibility.PROTECTED).annotate("@Override").addContent(
                        VarDeclaration.createListDeclaration("TrTag", "lines").markAsFinal()
                ).addContent(EMPTY_LINE).addContent(
                        new ForEach(beanName, beanVarName, new FunctionCall("getAll", beanName)).addContent(
                                new FunctionCall("add", "lines").byItself().addArgument(new FunctionCall("getTableLine").addArgument(beanVarName))
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("lines")
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getLineCount", "long").visibility(Visibility.PROTECTED).annotate("@Override").addContent(
                        new ReturnStatement(
                                new FunctionCall("getCount", beanName)
                        )
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration masterFunction = getMasterFunction("TableLine", "line", false);
        addFunctionCallsTo(masterFunction);
        masterFunction.addContent(new ReturnStatement("line"));
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        addTableCellGetterFunctions();
    }

    private void addFunctionCallsTo(final FunctionDeclaration functionDeclaration) {
        functionDeclaration.addContent(
                new IfBlock(new Condition("displayId")).addContent(getChildCall("id"))
        );
        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder()))
                functionDeclaration.addContent(getChildCall(column.getJavaName()));

        functionDeclaration.addContent(EMPTY_LINE);
    }

    private FunctionCall getChildCall(final String javaName) {
        return new FunctionCall("child", "line").byItself().addArgument(
                new FunctionCall("get" + Strings.capitalize(javaName) + "TableCell").addArgument(beanVarName)
        );
    }

    private void addTableCellGetterFunctions() {
        for (Column column: columns.getList())
            if (!column.isItemOrder()) {
                final FunctionCall getTableCellCall = new FunctionCall("getTableCell").addArgument(Strings.quickQuote(column.getJavaName()));
                if (column.hasAssociatedBean())
                    getTableCellCall.addArgument(
                            new FunctionCall("getHumanReadableTitle", column.getAssociatedBeanClass()).addArgument(
                                    new FunctionCall("get" + Strings.capitalize(column.getJavaName()), beanVarName)
                            )
                    );
                else {
                    final String prefix = column.getJavaType().equals("boolean") ? "is" : "get";
                    getTableCellCall.addArgument(new FunctionCall(prefix + Strings.capitalize(column.getJavaName()), beanVarName));
                }

                javaClass.addContent(
                        new FunctionDeclaration("get" + Strings.capitalize(column.getJavaName()) + "TableCell", "TdTag").visibility(Visibility.PROTECTED)
                                .addArgument(new FunctionArgument(beanName, beanVarName)).addContent(new ReturnStatement(getTableCellCall))
                ).addContent(EMPTY_LINE);
            }
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
