package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.ForEach;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.Increment;
import org.jcodegen.java.JavaCodeBlock;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.TernaryOperator;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.beanmaker.SourceFiles.chopId;

public class BaseExcelExportSourceFile extends BeanCodeForTabularView {

    private final String beanName;
    private final String beanVarName;

    public BaseExcelExportSourceFile(
            String beanName,
            String packageName,
            Columns columns,
            String tableName)
    {
        super(beanName, "ExcelExport", packageName, "Base", columns, tableName);

        this.beanName = beanName;
        beanVarName = getBeanVarName(beanName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.apache.poi.ss.usermodel.Row");
        importsManager.addImport("org.apache.poi.ss.usermodel.Sheet");

        importsManager.addImport("java.util.List");

        if (columns.hasLabels())
            importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("ExcelExport");
    }

    private void addTitleRowFunctions() {
        final FunctionDeclaration masterFunction = new FunctionDeclaration("addTitleRowTo").annotate("@Override")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Sheet", "sheet"))
                .addContent(
                        new VarDeclaration("Row", "titleRow", new FunctionCall("createRow", "sheet")
                                .addArgument(new TernaryOperator(
                                        new Condition("hasSuperTitleRow"), "1", "0")))
                                .markAsFinal()
                ).addContent(EMPTY_LINE).addContent(
                        new VarDeclaration("int", "cellCounter", "0")
                ).addContent(EMPTY_LINE);

        addTitleFunctionCallsTo(masterFunction);
        javaClass.addContent(masterFunction).addContent(EMPTY_LINE);

        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.isLabelReference())
                    addLabelTitleFunctions(chopId(column.getJavaName()));
                else
                    addTitleFunction(column.getJavaName());
            }
    }

    private void addTitleFunctionCallsTo(FunctionDeclaration masterFunction) {
        masterFunction.addContent(
                new IfBlock(new Condition("displayId")).addContent(
                        new FunctionCall("addIdTitleCell").byItself().addArguments("titleRow", "cellCounter++")
                )
        );

        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.isLabelReference())
                    masterFunction.addContent(getAddLabelTitleCall(chopId(column.getJavaName())));
                else
                    masterFunction.addContent(getCellTitleCall(column.getJavaName()));
            }
    }

    private JavaCodeBlock getAddLabelTitleCall(String labelJavaName) {
        return new Assignment(
                "cellCounter",
                new FunctionCall("add" + labelJavaName + "TitleCells")
                        .addArguments("titleRow", "cellCounter"));
    }

    private JavaCodeBlock getCellTitleCall(String javaName) {
        return new FunctionCall("add" + Strings.capitalize(javaName) + "TitleCell")
                .byItself()
                .addArguments("titleRow", "cellCounter++");
    }

    private void addLabelTitleFunctions(String labelJavaName) {
        addMainLabelTitleFunction(labelJavaName);
        addSecondaryLabelTitleFunction(labelJavaName);
    }

    private void addMainLabelTitleFunction(String labelJavaName) {
        javaClass.addContent(
                new FunctionDeclaration("add" + labelJavaName + "TitleCells", "int")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Row", "titleRow"))
                        .addArgument(new FunctionArgument("int", "cellNumber"))
                        .addContent(
                                new VarDeclaration("int", "currentCellNumber", "cellNumber - 1")
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new IfBlock(new Condition("displayAllLanguages")).addContent(
                                        new ForEach(
                                                "DbBeanLanguage",
                                                "dbBeanLanguage",
                                                new FunctionCall("getAllActiveLanguages", "Labels")
                                        ).addContent(
                                                getInternalLabelTitleCellFunctionCall(labelJavaName)
                                        )
                                ).elseClause(new ElseBlock().addContent(getInternalLabelTitleCellFunctionCall(labelJavaName)))
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(new ReturnStatement("currentCellNumber"))
        ).addContent(EMPTY_LINE);
    }

    private JavaCodeBlock getInternalLabelTitleCellFunctionCall(String labelJavaName) {
        return new FunctionCall("add" + labelJavaName + "TitleCell")
                .byItself()
                .addArguments("titleRow", "++currentCellNumber", "dbBeanLanguage");
    }

    private void addSecondaryLabelTitleFunction(String labelJavaName) {
        javaClass.addContent(
                new FunctionDeclaration("add" + labelJavaName + "TitleCell")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Row", "titleRow"))
                        .addArgument(new FunctionArgument("int", "cellNumber"))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new FunctionCall("addAdHocTitleCell")
                                        .byItself()
                                        .addArguments(
                                                "titleRow",
                                                getAdHocLabelTitleCellCallArgument(labelJavaName),
                                                "cellNumber")
                        )
        ).addContent(EMPTY_LINE);
    }

    private String getAdHocLabelTitleCellCallArgument(String labelJavaName) {
        return "resourceBundle.getString(\"id" + labelJavaName + "\") + \" \" + dbBeanLanguage.getCapIso()";
    }

    private void addTitleFunction(String javaName) {
        javaClass.addContent(
                new FunctionDeclaration("add" + Strings.capitalize(javaName) + "TitleCell")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Row", "titleRow"))
                        .addArgument(new FunctionArgument("int", "cellNumber"))
                        .addContent(
                                new FunctionCall("addTitleCell")
                                        .byItself()
                                        .addArguments("titleRow", Strings.quickQuote(javaName), "cellNumber")
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addDataFunctions() {
        addGetDataFunction();
        addDataRowsFunction();
        addDataCellFunctions();
    }

    private void addGetDataFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getData", "List<" + beanName  + ">")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ReturnStatement(new FunctionCall("getAll", beanName))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addDataRowsFunction() {
        final FunctionDeclaration dataRowsFunction =
                new FunctionDeclaration("addDataRowsTo")
                        .annotate("@Override")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Sheet", "sheet"))
                        .addContent(new VarDeclaration(
                                "int",
                                "rowCounter",
                                new TernaryOperator(new Condition("hasSuperTitleRow"), "1", "0")))
                        .addContent(new VarDeclaration("int", "cellCounter", "0"))
                        .addContent(EMPTY_LINE);

        final ForEach dataLoop = new ForEach(beanName, beanVarName, new FunctionCall("getData"))
                .addContent(new Increment("rowCounter").byItself())
                .addContent(
                        new VarDeclaration("Row", "row",
                                new FunctionCall("createRow", "sheet").addArgument("rowCounter")
                        ).markAsFinal()
                )
                .addContent(EMPTY_LINE)
                .addContent(new Assignment("cellCounter", "0"))
                .addContent(EMPTY_LINE)
                .addContent(
                        new IfBlock(new Condition("displayId"))
                                .addContent(
                                        new FunctionCall("addIdCell")
                                                .byItself()
                                                .addArguments("row", beanVarName, "cellCounter++")
                                )
                );

        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.isLabelReference())
                    dataLoop.addContent(getLabelDataFunctionCall(chopId(column.getJavaName())));
                else
                    dataLoop.addContent(getDataFunctionCall(column.getJavaName()));
            }

        dataRowsFunction.addContent(dataLoop)
                .addContent(EMPTY_LINE)
                .addContent(
                        new FunctionCall("autosizeColumns")
                                .byItself()
                                .addArguments("sheet", "cellCounter")
                );

        javaClass.addContent(dataRowsFunction).addContent(EMPTY_LINE);
    }

    private JavaCodeBlock getLabelDataFunctionCall(String labelJavaName) {
        return new Assignment(
                "cellCounter",
                new FunctionCall("add" + labelJavaName + "Cells")
                        .addArguments("row", beanVarName, "cellCounter"));
    }

    private JavaCodeBlock getDataFunctionCall(String javaName) {
        return new FunctionCall("add" + Strings.capitalize(javaName) + "Cell")
                .byItself()
                .addArguments("row", beanVarName, "cellCounter++");
    }

    private void addDataCellFunctions() {
        for (Column column: columns.getList())
            if (!(column.isId() || column.isItemOrder())) {
                if (column.getJavaType().equals("boolean"))
                    addBooleanCellFunction(column.getJavaName());
                else if (column.getJavaType().equals("int") || column.getJavaType().equals("long")) {
                    if (column.isLabelReference())
                        addLabelDataCellsFunctions(column.getJavaName());
                    else if (column.isFileReference())
                        addFileDataCellFunction(column.getJavaName());
                    else if (column.hasAssociatedBean())
                        addAssociatedBeanCellFunction(column.getJavaName(), column.getAssociatedBeanClass());
                    else
                        addDataCellFunction(column.getJavaName());
                } else if (column.getJavaType().equals("String"))
                    addDataCellFunction(column.getJavaName());
                else if (column.getJavaType().equals("Date"))
                    addDataCellFunction(column.getJavaName());
                else if (column.getJavaType().equals("Time"))
                    addDataCellFunction(column.getJavaName());
                else if (column.getJavaType().equals("Timestamp"))
                    addDataCellFunction(column.getJavaName());
                else if (column.getJavaType().equals("Money"))
                    addDataCellFunction(column.getJavaName());
                else
                    throw new IllegalStateException("Could not identify field type for : " + column);
            }
    }

    private void addBooleanCellFunction(final String javaName) {
        final FunctionDeclaration dataCellFunction = getDataFunctionDeclaration(javaName);
        dataCellFunction.addContent(
                getAddDataCellFunctionCall()
                        .addArgument(
                                new FunctionCall("getYesOrNoRepresentation")
                                        .addArgument(new FunctionCall("is" + Strings.capitalize(javaName), beanVarName))
                )
        );
        javaClass.addContent(dataCellFunction).addContent(EMPTY_LINE);
    }

    private FunctionDeclaration getDataFunctionDeclaration(final String javaName) {
        return getDataFunctionDeclaration(javaName, false);
    }

    private FunctionCall getAddDataCellFunctionCall() {
        return new FunctionCall("addDataCell")
                .byItself()
                .addArguments("row", "cellNumber");
    }

    private FunctionDeclaration getDataFunctionDeclaration(final String javaName, final boolean forLabels) {
        final String baseFunctionName = "add" + Strings.capitalize(javaName) + "Cell";
        final FunctionDeclaration functionDeclaration;
        if (forLabels)
            functionDeclaration = new FunctionDeclaration(baseFunctionName + "s", "int");
        else
            functionDeclaration = new FunctionDeclaration(baseFunctionName);

        functionDeclaration
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Row", "row"))
                .addArgument(new FunctionArgument(beanName, beanVarName))
                .addArgument(new FunctionArgument("int", "cellNumber"));

        return functionDeclaration;
    }

    private void addAssociatedBeanCellFunction(final String javaName, final String associatedBeanClass) {
        final FunctionDeclaration dataCellFunction = getDataFunctionDeclaration(javaName);
        dataCellFunction.addContent(
                getAddDataCellFunctionCall()
                        .addArgument(
                                new FunctionCall("getHumanReadableTitle", associatedBeanClass)
                                        .addArgument(new FunctionCall("get" + Strings.capitalize(javaName), beanVarName))
                )
        );
        javaClass.addContent(dataCellFunction).addContent(EMPTY_LINE);
    }

    private void addLabelDataCellsFunctions(final String javaName) {
        final String labelJavaName = chopId(javaName);
        final String labelFunctionName = "add" + labelJavaName  + "Cell";
        final FunctionDeclaration labelsFunction = getDataFunctionDeclaration(labelJavaName, true);

        labelsFunction.addContent(
                new VarDeclaration("int", "currentCellNumber", "cellNumber - 1")
        ).addContent(EMPTY_LINE).addContent(
                new IfBlock(new Condition("displayAllLanguages")).addContent(
                        new ForEach(
                                "DbBeanLanguage",
                                "dbBeanLanguage",
                                new FunctionCall("getAllActiveLanguages", "Labels")
                        ).addContent(getMainLabelCellFunctionCall(labelFunctionName))
                ).elseClause(new ElseBlock().addContent(getMainLabelCellFunctionCall(labelFunctionName)))
        ).addContent(EMPTY_LINE).addContent(
                new ReturnStatement("currentCellNumber")
        );

        javaClass.addContent(labelsFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration(labelFunctionName)
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Row", "row"))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addArgument(new FunctionArgument("int", "cellNumber"))
                        .addContent(
                                getAddDataCellFunctionCall()
                                        .addArgument(
                                                new FunctionCall("get" + labelJavaName, beanVarName)
                                                        .addArgument("dbBeanLanguage")
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addFileDataCellFunction(String javaName) {
        final FunctionDeclaration dataCellFunction = getDataFunctionDeclaration(javaName);
        dataCellFunction.addContent(
                getAddDataCellFunctionCall()
                        .addArgument(
                                new FunctionCall("getFilename", "LocalFiles")
                                        .addArgument(
                                                new FunctionCall("get" + Strings.capitalize(javaName), beanVarName)
                                        )
                )
        );
        javaClass.addContent(dataCellFunction).addContent(EMPTY_LINE);
    }

    private FunctionCall getMainLabelCellFunctionCall(final String labelFunctionName) {
        return new FunctionCall(labelFunctionName)
                .byItself()
                .addArguments("row", "dbBeanLanguage", beanVarName, "++currentCellNumber");
    }

    private void addDataCellFunction(final String javaName) {
        final FunctionDeclaration dataCellFunction = getDataFunctionDeclaration(javaName);
        dataCellFunction.addContent(
                getAddDataCellFunctionCall()
                        .addArgument(
                                new FunctionCall("get" + Strings.capitalize(javaName), beanVarName)
                        )
        );
        javaClass.addContent(dataCellFunction).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addConstructor();
        addTitleRowFunctions();
        addDataFunctions();
    }
}
