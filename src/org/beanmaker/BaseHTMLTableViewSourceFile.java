package org.beanmaker;

import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.Expression;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.dbbeans.util.Strings.capitalize;
import static org.dbbeans.util.Strings.quickQuote;

public class BaseHTMLTableViewSourceFile extends ViewCode {

    public BaseHTMLTableViewSourceFile(
            final String beanName,
            final String packageName,
            final Columns columns,
            final String tableName)
    {
        super(beanName, packageName, "HTMLTableViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("java.util.List");

        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.DbBeanViewInterface");
        importsManager.addImport("org.beanmaker.util.HtmlTableHelper");
    }

    @Override
    protected void addProperties() {
        super.addProperties();
        newLine();
        javaClass.addContent(
                VarDeclaration
                        .declareAndInit("HtmlTableHelper", "htmlTableHelper")
                        .visibility(Visibility.PROTECTED)
        );
    }

    private void addHTMLTableGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getHtmlTable", "String").addContent(
                        ifNotDataOK(true).addContent(
                                ExceptionThrow.getThrowExpression(
                                        "IllegalArgumentException",
                                        "Cannot display bad data")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement(
                                new FunctionCall("getTable", "htmlTableHelper")
                                        .addArgument(quickQuote(beanName))
                                        .addArgument(getId())
                                        .addArgument(new FunctionCall("getHtmlTableRows"))
                        )
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getHtmlTableRowsFunction =
                new FunctionDeclaration(
                        "getHtmlTableRows",
                        "List<HtmlTableHelper.Row>")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                VarDeclaration
                                        .createListDeclaration("HtmlTableHelper.Row", "rows")
                                        .markAsFinal()
                        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();
                getHtmlTableRowsFunction.addContent(
                        new FunctionCall("add", "rows")
                                .byItself()
                                .addArgument(new FunctionCall("get" + capitalize(field) + "Row"))
                );
            }
        }

        getHtmlTableRowsFunction.addContent(EMPTY_LINE).addContent(
                new ReturnStatement("rows")
        );

        javaClass.addContent(getHtmlTableRowsFunction).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();

                final ObjectCreation rowCreation = new ObjectCreation("HtmlTableHelper.Row")
                        .addArgument(quickQuote(field))
                        .addArgument(new FunctionCall("get" + capitalize(field) + "Label", beanVarName));

                addDataArgument(column, rowCreation);

                rowCreation.addArgument(
                        new FunctionCall("shouldUseStrongInsteadOfTh", "htmlTableHelper")
                );

                javaClass.addContent(
                        new FunctionDeclaration("get" + capitalize(field) + "Row", "HtmlTableHelper.Row")
                                .visibility(Visibility.PROTECTED)
                                .addContent(
                                        new ReturnStatement(rowCreation)
                                )
                ).addContent(EMPTY_LINE);
            }
        }
    }

    private void addToStringFunction() {
        javaClass.addContent(
                new FunctionDeclaration("toString", "String").annotate("@Override").addContent(
                        ifNotDataOK(true).addContent(
                                ExceptionThrow.getThrowExpression(
                                        "IllegalArgumentException",
                                        "Cannot display bad data")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement(
                                new FunctionCall("toString", new FunctionCall("getTextTableRow"))
                        )
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getTextTableRowFunction =
                new FunctionDeclaration(
                        "getTextTableRow",
                        "StringBuilder")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                VarDeclaration.declareAndInitFinal("StringBuilder", "rows")
                        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();
                getTextTableRowFunction.addContent(
                        new FunctionCall("append", "rows")
                                .byItself()
                                .addArgument(new FunctionCall("get" + capitalize(field) + "TextRow"))
                );
            }
        }

        getTextTableRowFunction.addContent(EMPTY_LINE).addContent(
                new ReturnStatement("rows")
        );

        javaClass.addContent(getTextTableRowFunction).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();

                final FunctionCall rowCreation = new FunctionCall("getTextRow","HtmlTableHelper")
                        .addArgument(new FunctionCall("get" + capitalize(field) + "Label", beanVarName));

                addDataArgument(column, rowCreation);

                javaClass.addContent(
                        new FunctionDeclaration("get" + capitalize(field) + "TextRow", "String")
                                .visibility(Visibility.PROTECTED)
                                .addContent(
                                        new ReturnStatement(rowCreation)
                                )
                ).addContent(EMPTY_LINE);
            }
        }
    }

    private void addDataArgument(final Column column, final Expression expression) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (type.equals("boolean"))
            expression.addArgument(
                    new FunctionCall("get" + capitalize(field) + "Val", beanVarName)
            );

        else if (type.equals("int") || type.equals("long")) {
            if (column.isLabelReference())
                expression.addArgument(
                        new FunctionCall("get" + SourceFiles.chopId(field), beanVarName)
                                .addArgument("dbBeanLanguage")
                );
            else if (column.isFileReference())
                expression.addArgument(getFilenameFunctionCall(beanVarName, field));
            else {
                if (field.startsWith("id"))
                    expression.addArgument(
                            new FunctionCall("getHumanReadableTitle", column.getAssociatedBeanClass())
                                    .addArgument(getFieldValue(field))
                    );
                else {
                    if (type.equals("int"))
                        expression.addArgument(
                                new FunctionCall("toString", "Integer")
                                        .addArgument(getFieldValue(field))
                        );
                    else
                        expression.addArgument(
                                new FunctionCall("toString", "Long")
                                        .addArgument(getFieldValue(field))
                        );
                }
            }
        }

        else if (type.equals("String"))
            expression.addArgument(getFieldValue(field));

        else if (JAVA_TEMPORAL_TYPES.contains(type))
            expression.addArgument(
                    new FunctionCall("get" + capitalize(field) + "Formatted", beanVarName)
            );

        else if (type.equals("Money"))
            expression.addArgument(
                    new FunctionCall("toString", getFieldValue(field))
            );

        else
            throw new IllegalStateException("Unknown type " + type);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        javaClass.markAsAbstract().implementsInterface("DbBeanViewInterface");
        addViewPrelude(true, true);
        addHTMLTableGetter();
        addToStringFunction();
    }
}
