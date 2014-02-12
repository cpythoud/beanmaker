package org.beanmaker;

import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.dbbeans.util.Strings.capitalize;
import static org.dbbeans.util.Strings.quickQuote;

public class BaseHTMLTableViewSourceFile extends ViewCode {

    public BaseHTMLTableViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "HTMLTableViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("java.util.List");

        importsManager.addImport("org.beanmaker.util.HtmlTableHelper");
    }

    private void addHTMLTableGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getHtmlTable", "String").addContent(
                        ifNotDataOK(true).addContent(
                                ExceptionThrow.getThrowExpression("IllegalArgumentException", "Cannot display bad data")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        VarDeclaration.declareAndInitFinal("StringBuilder", "buf")
                ).addContent(
                        new FunctionCall("table", "HtmlTableHelper").byItself()
                                .addArguments("buf", quickQuote(beanName))
                                .addArgument(getId())
                                .addArgument(new FunctionCall("getHtmlTableRows"))
                ).addContent(
                        new ReturnStatement(new FunctionCall("toString", "buf"))
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getHtmlTableRowsFunction = new FunctionDeclaration("getHtmlTableRows", "List<HtmlTableHelper.Row>").visibility(Visibility.PROTECTED).addContent(
                VarDeclaration.createListDeclaration("HtmlTableHelper.Row", "rows").markAsFinal()
        );

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();

                final ObjectCreation rowCreation = new ObjectCreation("HtmlTableHelper.Row")
                        .addArgument(quickQuote(field))
                        .addArgument(new FunctionCall("get" + capitalize(field) + "Label", beanVarName));

                if (type.equals("boolean"))
                    rowCreation.addArgument(
                            new FunctionCall("get" + capitalize(field) + "Val", beanVarName)
                    );

                else if (type.equals("int") || type.equals("long")) {
                    if (field.startsWith("id"))
                        rowCreation.addArgument(
                                new FunctionCall("getHumanReadableTitle", column.getAssociatedBeanClass()).addArgument(getFieldValue(field))
                        );
                    else {
                        if (type.equals("int"))
                            rowCreation.addArgument(
                                    new FunctionCall("toString", "Integer").addArgument(getFieldValue(field))
                            );
                        else
                            rowCreation.addArgument(
                                    new FunctionCall("toString", "Long").addArgument(getFieldValue(field))
                            );
                    }
                }

                else if (type.equals("String"))
                    rowCreation.addArgument(getFieldValue(field));

                else if (JAVA_TEMPORAL_TYPES.contains(type))
                    rowCreation.addArgument(
                            new FunctionCall("get" + capitalize(field) + "Formatted", beanVarName)
                    );

                else if (type.equals("Money"))
                    rowCreation.addArgument(
                            new FunctionCall("toString", getFieldValue(field))
                    );

                else
                    throw new IllegalStateException("Unknown type " + type);

                getHtmlTableRowsFunction.addContent(
                        new FunctionCall("add", "rows").byItself().addArgument(rowCreation)
                );
            }
        }

        getHtmlTableRowsFunction.addContent(EMPTY_LINE).addContent(
                new ReturnStatement("rows")
        );

        javaClass.addContent(getHtmlTableRowsFunction).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        javaClass.markAsAbstract();
        addViewPrelude();
        addHTMLTableGetter();
    }
}