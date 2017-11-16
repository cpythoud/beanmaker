package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import org.dbbeans.util.Strings;

public abstract class ViewCode extends BeanCodeWithDBInfo {

    public ViewCode(
            final String beanName,
            final String packageName,
            final String nameExtension,
            final Columns columns,
            final String tableName)
    {
        super(beanName, packageName, nameExtension, columns, tableName);
    }

    protected void addViewPrelude() {
        addViewPrelude(false, false);
    }

    protected void addViewPrelude(final boolean withLocale, final boolean localeOnObjectOnly) {
        addProperties();
        newLine();
        addEmptyConstructor();
        newLine();
        addConstructorWithBean();
        newLine();
        addConstructorWithBeanAndLanguage();
        newLine();
        addBeanGetter();
        newLine();
        addIdFunctions();
        newLine();
        addSetLanguageFunction(withLocale, localeOnObjectOnly);
        newLine();
    }

    protected void addProperties() {
        javaClass.addContent(
                new VarDeclaration(beanName, beanVarName)
                        .markAsFinal()
                        .visibility(Visibility.PROTECTED)
        ).addContent(
                new VarDeclaration("DbBeanLanguage", "dbBeanLanguage")
                        .visibility(Visibility.PROTECTED)
        );
    }

    protected void addEmptyConstructor() {
        javaClass.addContent(
                javaClass.createConstructor().addContent(
                        new FunctionCall("this").addArgument(new ObjectCreation(beanName)).byItself()
                )
        );
    }

    protected void addConstructorWithBean() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addContent(
                                new FunctionCall("this")
                                        .addArguments(beanVarName, "null")
                                        .byItself()
                        )
        );
    }

    protected void addConstructorWithBeanAndLanguage() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new Assignment("this." + beanVarName, beanVarName)
                        )
                        .addContent(
                                new FunctionCall("setLanguage")
                                        .addArgument("dbBeanLanguage")
                                        .byItself()
                        )
                        /*.addContent(
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
                        )*/
        );
    }

    protected void addBeanGetter() {
        javaClass.addContent(
                new FunctionDeclaration("get" + beanName, beanName).addContent(
                        new ReturnStatement(beanVarName)
                )
        );
    }

    protected FunctionCall getId() {
        return new FunctionCall("getId", beanVarName);
    }

    protected FunctionCall getFieldValue(final String field) {
        return new FunctionCall("get" + Strings.capitalize(field), beanVarName);
    }

    protected FunctionCall getLabelArgument(final String field) {
        return new FunctionCall("get" + Strings.capitalize(field) + "Label", beanVarName);
    }

    private void addIdFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("resetId").addContent(
                        new FunctionCall("resetId", beanVarName).byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setResetId")
                        .annotate("@Override")
                        .addArgument(new FunctionArgument("String", "dummy"))
                        .addContent(new FunctionCall("resetId").byItself())
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setId")
                        .annotate("@Override")
                        .addArgument(new FunctionArgument("long", "id"))
                        .addContent(
                                new FunctionCall("setId", beanVarName).addArgument("id").byItself()
                        )
        );
    }

    private void addSetLanguageFunction(final boolean withLocale, final boolean localeOnObjectOnly) {
        final ElseBlock languageNotNull =
                new ElseBlock()
                        .addContent(
                                new Assignment(
                                        "this.dbBeanLanguage",
                                        new FunctionCall("getCopy", "Labels")
                                                .addArgument("dbBeanLanguage"))
                        );

        if (withLocale) {
            final FunctionCall setLocaleCall;
            if (localeOnObjectOnly)
                setLocaleCall = new FunctionCall("setLocale", beanVarName);
            else
                setLocaleCall = new FunctionCall("setLocale");

            languageNotNull.addContent(
                    setLocaleCall
                            .byItself()
                            .addArgument(new FunctionCall("getLocale", "dbBeanLanguage"))
            );
        }

        javaClass.addContent(
                new FunctionDeclaration("setLanguage")
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new IfBlock(new Condition(new Comparison("dbBeanLanguage", "null")))
                                        .addContent(
                                                new Assignment("this.dbBeanLanguage", "null")
                                        )
                                        .elseClause(languageNotNull)
                        )
        );
    }
}
