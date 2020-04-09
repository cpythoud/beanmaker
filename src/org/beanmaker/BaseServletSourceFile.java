package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.Condition;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.TernaryOperator;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class BaseServletSourceFile extends BeanCode {

    private final Columns columns;

    public BaseServletSourceFile(final String beanName, final String packageName, final Columns columns) {
        super(beanName, packageName, "ServletBase");
        this.columns = columns;

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.ChangeOrderDirection");
        importsManager.addImport("org.beanmaker.util.DbBeanHTMLViewInterface");
        importsManager.addImport("org.beanmaker.util.DbBeanInterface");

        importsManager.addImport("javax.servlet.ServletException");
        importsManager.addImport("javax.servlet.http.HttpServletRequest");
    }

    private void addHTMLViewFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getHTMLView", "DbBeanHTMLViewInterface")
                        .annotate("@Override")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("long", "id"))
                        .addArgument(new FunctionArgument("HttpServletRequest", "request"))
                        .addException("ServletException")
                        .addContent(VarDeclaration.declareAndInit(beanName, beanVarName))
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new IfBlock(new Condition("id > 0"))
                                        .addContent(new FunctionCall("setId", beanVarName)
                                                .addArgument("id")
                                                .byItself()))
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement(
                                        new ObjectCreation(beanName + "HTMLView")
                                                .addArgument(beanVarName)
                                                .addArgument(new FunctionCall("getLanguage")
                                                        .addArgument(new FunctionCall("getSession", "request"))))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addBeanIdFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getSubmitBeanId", "long")
                        .annotate("@Override")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("HttpServletRequest", "request"))
                        .addContent(
                                new ReturnStatement(new FunctionCall("getBeanId")
                                        .addArguments("request", Strings.quickQuote("submitted" + beanName)))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addInstanceFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getInstance", "DbBeanInterface")
                        .annotate("@Override")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("long", "id"))
                        .addContent(new ReturnStatement(new ObjectCreation(beanName).addArgument("id")))
        ).addContent(EMPTY_LINE);
    }

    private void addChangeOrderFunction() {
        FunctionDeclaration functionDeclaration = new FunctionDeclaration("changeOrder", "String")
                .annotate("@Override")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("long", "id"))
                .addArgument(new FunctionArgument("ChangeOrderDirection", "direction"))
                .addArgument(new FunctionArgument("long", "companionId"));

        if (columns.hasItemOrder())
            functionDeclaration.addContent(
                    new ReturnStatement(
                            new FunctionCall("changeOrder")
                                    .addArgument(new ObjectCreation(beanName).addArgument("id"))
                                    .addArgument("direction")
                                    .addArgument(new TernaryOperator(
                                            new Condition("companionId > 0"),
                                            new ObjectCreation(beanName).addArgument("companionId"),
                                            "null"))
                    )
            );
        else
            functionDeclaration.addContent(
                    new ExceptionThrow("UnsupportedOperationException")
                            .addArgument(Strings.quickQuote(
                                    beanName + " beans have no ordering. (No itemOrder field present.)"))
            );

        javaClass.addContent(functionDeclaration).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass("BaseServlet").markAsAbstract().addContent(EMPTY_LINE);

        addImports();
        addHTMLViewFunction();
        addBeanIdFunction();
        addInstanceFunction();
        addChangeOrderFunction();
    }
}
