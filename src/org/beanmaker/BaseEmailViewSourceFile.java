package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class BaseEmailViewSourceFile extends ViewCode {

    public BaseEmailViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "EmailViewBase", columns, tableName);

        createSourceCode();
    }

    @Override
    protected void addProperties() {
        super.addProperties();
        javaClass.addContent(
                new VarDeclaration(beanName + "HTMLTableView", "tableView").markAsFinal().visibility(Visibility.PRIVATE)
        );
    }

    @Override
    protected void addConstructorWithBean() {
        javaClass.addContent(
                javaClass.createConstructor().addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                        new Assignment("this." + beanVarName, beanVarName)
                ).addContent(
                        new Assignment("tableView", new ObjectCreation(beanName + "HTMLTableView").addArgument(beanVarName))
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
        importsManager.addImport("org.dbbeans.util.Email");
        javaClass.markAsAbstract();
        addViewPrelude();
        addSendEmail();
    }
}
