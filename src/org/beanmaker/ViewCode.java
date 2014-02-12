package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;
import org.dbbeans.util.Strings;

public abstract class ViewCode extends BeanCodeWithDBInfo {

    public ViewCode(final String beanName, final String packageName, final String nameExtension, final Columns columns, final String tableName) {
        super(beanName, packageName, nameExtension, columns, tableName);
    }

    protected void addViewPrelude() {
        addProperties();
        newLine();
        addEmptyConstructor();
        newLine();
        addConstructorWithBean();
        newLine();
        addBeanGetter();
        newLine();
        newLine();
    }

    protected void addProperties() {
        javaClass.addContent(
                new VarDeclaration(beanName, beanVarName).markAsFinal().visibility(Visibility.PRIVATE)
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
                javaClass.createConstructor().addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                        new Assignment("this." + beanVarName, beanVarName)
                )
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
}