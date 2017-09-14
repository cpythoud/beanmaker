package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class BaseEmailViewSourceFile extends ViewCode {

    public BaseEmailViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "EmailViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.dbbeans.util.Email");

        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.DbBeanViewInterface");
    }

    @Override
    protected void addProperties() {
        super.addProperties();
        javaClass.addContent(
                new VarDeclaration(beanName + "HTMLTableView", "tableView").markAsFinal().visibility(Visibility.PRIVATE)
        );
    }

    @Override
    protected void addConstructorWithBeanAndLanguage() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new Assignment("this." + beanVarName, beanVarName)
                        )
                        .addContent(
                                new IfBlock(new Condition(new Comparison("dbBeanLanguage", "null")))
                                        .addContent(
                                                new Assignment("this.dbBeanLanguage", "null")
                                        ).elseClause(
                                        new ElseBlock()
                                                .addContent(
                                                        new Assignment(
                                                                "this.dbBeanLanguage",
                                                                new FunctionCall("getCopy", "Labels")
                                                                        .addArgument("dbBeanLanguage"))
                                                )
                                                .addContent(
                                                        new FunctionCall("setLocale", beanVarName)
                                                                .byItself()
                                                                .addArgument(new FunctionCall("getLocale", "dbBeanLanguage"))
                                                ))
                        )
                        .addContent(
                                new Assignment("tableView", new ObjectCreation(beanName + "HTMLTableView")
                                        .addArgument(beanVarName))
                        )
        );
    }

    private void addSendEmail() {
        javaClass.addContent(
                new FunctionDeclaration("sendEmail").addArgument(new FunctionArgument("Email", "email")).addContent(
                        ifNotDataOK(true).addContent(
                                ExceptionThrow.getThrowExpression("IllegalArgumentException", "Cannot e-mail bad data")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionCall("setHtmlMainText", "email").byItself().addArgument(new FunctionCall("getHtmlTable", "tableView"))
                ).addContent(
                        new FunctionCall("send", "email").byItself()
                )
        ).addContent(EMPTY_LINE);
	}

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        addImports();
        javaClass.markAsAbstract().implementsInterface("DbBeanViewInterface");
        addViewPrelude();
        addSendEmail();
    }
}
