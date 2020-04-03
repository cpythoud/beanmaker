package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.Condition;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

public class BaseServletSourceFile extends BeanCode {

    public BaseServletSourceFile(final String beanName, final String packageName) {
        super(beanName, packageName, "ServletBase");

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanHTMLViewInterface");
        importsManager.addImport("org.beanmaker.util.DbBeanInterface");
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");

        importsManager.addImport("javax.servlet.http.HttpServletRequest");
    }

    private void addHTMLViewFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getHTMLView", "DbBeanHTMLViewInterface")
                        .annotate("@Override")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("long", "id"))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "language"))
                        .addContent(VarDeclaration.declareAndInit(beanName, beanVarName))
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new IfBlock(new Condition("id > 0"))
                                        .addContent(new FunctionCall("setId", beanVarName)
                                                .addArgument("id")
                                                .byItself()))
                        .addContent(EMPTY_LINE)
                        .addContent(new ReturnStatement(
                                new ObjectCreation(beanName + "HTMLView").addArguments(beanVarName, "language")))
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

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass("BaseServlet").markAsAbstract().addContent(EMPTY_LINE);

        addImports();
        addHTMLViewFunction();
        addBeanIdFunction();
        addInstanceFunction();
    }
}
