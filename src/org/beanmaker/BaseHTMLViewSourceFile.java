package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.Expression;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.dbbeans.util.Strings.capitalize;
import static org.dbbeans.util.Strings.quickQuote;
import static org.dbbeans.util.Strings.uncapitalize;

public class BaseHTMLViewSourceFile extends ViewCode {

    private static final int TEXTAREA_THRESHOLD = 1000;

    public BaseHTMLViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "HTMLViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.BaseHTMLView");
        importsManager.addImport("org.beanmaker.util.DbBeanHTMLViewInterface");
        importsManager.addImport("org.beanmaker.util.ErrorMessage");

        importsManager.addImport("javax.servlet.ServletRequest");

        importsManager.addImport("org.beanmaker.util.HFHParameters");

        importsManager.addImport("org.jcodegen.html.FormTag");
        importsManager.addImport("org.jcodegen.html.Tag");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("BaseHTMLView").implementsInterface("DbBeanHTMLViewInterface");
    }

    @Override
    protected void addConstructorWithBean() {
        javaClass.addContent(
                javaClass.createConstructor().addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                        new FunctionCall("super").addArgument(quickQuote(bundleName + "-HTML")).byItself()
                ).addContent(
                        new Assignment("this." + beanVarName, beanVarName)
                )
        );
    }

    private void addIdFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("resetId").addContent(
                        new FunctionCall("resetId", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setResetId")
                        .addArgument(new FunctionArgument("String", "dummy"))
                        .addContent(new FunctionCall("resetId").byItself())
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setId").addArgument(new FunctionArgument("long", "id")).addContent(
                        new FunctionCall("setId", beanVarName).addArgument("id").byItself()
                )
        ).addContent(EMPTY_LINE).addContent(EMPTY_LINE);
    }

    private void addChecksForRequiredFields() {
        for (Column column: columns.getList())
            if (!column.isSpecial())
                javaClass.addContent(
                        new FunctionDeclaration("is" + capitalize(column.getJavaName() + "RequiredInHtmlForm"), "boolean")
                                .addContent(new ReturnStatement(new FunctionCall("is" + capitalize(column.getJavaName() + "Required"), beanVarName)))
                ).addContent(EMPTY_LINE);
    }

    private void addHTMLFormGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getHtmlForm", "String").addContent(
                        new ReturnStatement("getHtmlFormTag().toString()")
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getHtmlFormFunction = new FunctionDeclaration("getHtmlFormTag", "FormTag").addContent(
                new VarDeclaration("FormTag", "form", new FunctionCall("getFormStart")).markAsFinal()
        ).addContent(
                new FunctionCall("composeHiddenSubmitField").byItself()
                        .addArgument("form")
        );

        for (Column column: columns.getList()) {
            final String field = column.getJavaName();
            if (!field.equals("id") && !field.equals("lastUpdate") && !field.equals("modifiedBy") && !field.equals("itemOrder"))
                getHtmlFormFunction.addContent(getFieldHtmlFormFunctionCall(column));
        }

        getHtmlFormFunction.addContent(
                new FunctionCall("composeAdditionalHtmlFormFields").byItself()
                        .addArgument("form")
        )/*.addContent(
                new IfBlock(new Condition("captchaControl"))
                        .addContent(new FunctionCall("composeCaptchaField").byItself().addArgument("buf"))
        )*/.addContent(
                new FunctionCall("composeButtons").byItself()
                        .addArgument("form")
        ).addContent(EMPTY_LINE).addContent(
                new ReturnStatement("form")
        );

        javaClass.addContent(getHtmlFormFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getFormStart", "FormTag")
                        .visibility(Visibility.PROTECTED)
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getForm", "htmlFormHelper")
                                                .addArgument(quickQuote(beanName))
                                                .addArgument(new FunctionCall("getId", beanVarName))
                                )
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("composeHiddenSubmitField")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
                        .addContent(
                                new FunctionCall("child", "form").byItself()
                                        .addArgument(new FunctionCall("getHiddenSubmitInput", "htmlFormHelper")
                                                .addArgument(quickQuote(beanName))
                                                .addArgument(new FunctionCall("getId", beanVarName)))
                        )
        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            final String field = column.getJavaName();
            if (!field.equals("id")
                    && !field.equals("lastUpdate")
                    && !field.equals("modifiedBy")
                    && !field.equals("itemOrder")) {
                addFieldHtmlFormFunctions(column);
                //javaClass.addContent(getFieldHtmlFormFunction(column)).addContent(EMPTY_LINE);
            }
        }

        javaClass.addContent(
                new FunctionDeclaration("composeAdditionalHtmlFormFields").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
        ).addContent(EMPTY_LINE);

        /*javaClass.addContent(
                new FunctionDeclaration("composeCaptchaField").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("StringBuilder", "buf"))
                        .addContent(new FunctionCall("captcha", "htmlFormHelper").byItself()
                                .addArguments("buf", quickQuote("captchaValue"))
                                .addArgument(getId())
                                .addArgument(new FunctionCall("getString", "resourceBundle").addArgument(quickQuote("captchaFieldName")))
                                .addArgument(quickQuote("/captcha.jpg")))
        ).addContent(EMPTY_LINE);*/

        javaClass.addContent(
                new FunctionDeclaration("composeButtons").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
                        .addContent(new FunctionCall("composeSubmitButton").byItself().addArgument("form"))
                        .addContent(new FunctionCall("composeResetButton").byItself().addArgument("form"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("composeSubmitButton").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
                        .addContent(
                                new FunctionCall("child", "form").byItself()
                                        .addArgument(new FunctionCall("getSubmitButton", "htmlFormHelper")
                                                .addArgument(quickQuote(beanName))
                                                .addArgument(getId())
                                                .addArgument(new FunctionCall("getString", "resourceBundle").addArgument(quickQuote("submit_button"))))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("composeResetButton").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
        ).addContent(EMPTY_LINE);
	}

    private FunctionCall getFieldHtmlFormFunctionCall(final Column column) {
        return new FunctionCall("compose" + capitalize(column.getJavaName()) + "FormElement").addArgument("form").byItself();
    }

    /*private FunctionDeclaration getFieldHtmlFormFunction(final Column column) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (type.equals("boolean"))
            return getCheckboxFormElement(field);

        if (type.equals("int") || type.equals("long")) {
            if (field.startsWith("id") && column.hasAssociatedBean())
                return getSelectForAssociatedBeanFunction(column);

            return getInputFormElement("NUMBER", field);
        }

        if (type.equals("String")) {
            if (column.getDisplaySize() < TEXTAREA_THRESHOLD) {
                if (field.equalsIgnoreCase("email") || field.equalsIgnoreCase("e-mail"))
                    return getInputFormElement("EMAIL", field);
                return getInputFormElement("TEXT", field);
            } else {
                return getTextAreaFormElement(field);
            }
        }

        if (type.equals("Date"))
            return getInputFormElement("DATE", field);

        if (type.equals("Time"))
            return getInputFormElement("TIME", field);

        if (type.equals("Timestamp"))
            return getInputFormElement("DATETIME", field);

        if (type.equals("Money"))
            return getInputFormElement("money", field);

        throw new IllegalStateException("Unknown and unsupported java type: " + type);
    }*/

    private void addFieldHtmlFormFunctions(final Column column) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (type.equals("boolean")) {
            javaClass.addContent(getCheckboxFormElement(field)).addContent(EMPTY_LINE);
            return;
        }

        if (type.equals("int") || type.equals("long")) {
            if (field.startsWith("id") && column.hasAssociatedBean())
                javaClass.addContent(getSelectForAssociatedBeanFunction(column)).addContent(EMPTY_LINE);
            else
                addInputFormElement("NUMBER", field);
                //javaClass.addContent(getInputFormElement("NUMBER", field)).addContent(EMPTY_LINE);
            return;
        }

        if (type.equals("String")) {
            if (column.getDisplaySize() < TEXTAREA_THRESHOLD) {
                if (field.equalsIgnoreCase("email") || field.equalsIgnoreCase("e-mail"))
                    addInputFormElement("EMAIL", field);
                    //javaClass.addContent(getInputFormElement("EMAIL", field)).addContent(EMPTY_LINE);
                else
                    addInputFormElement("TEXT", field);
                    //javaClass.addContent(getInputFormElement("TEXT", field)).addContent(EMPTY_LINE);
            } else {
                javaClass.addContent(getTextAreaFormElement(field)).addContent(EMPTY_LINE);
            }
            return;
        }

        if (type.equals("Date")) {
            addInputFormElement("DATE", field);
            //javaClass.addContent(getInputFormElement("DATE", field)).addContent(EMPTY_LINE);
            return;
        }

        if (type.equals("Time")) {
            addInputFormElement("TIME", field);
            //javaClass.addContent(getInputFormElement("TIME", field)).addContent(EMPTY_LINE);
            return;
        }

        if (type.equals("Timestamp")) {
            addInputFormElement("DATETIME", field);
            //javaClass.addContent(getInputFormElement("DATETIME", field)).addContent(EMPTY_LINE);
            return;
        }

        if (type.equals("Money")) {
            addInputFormElement("money", field);
            //javaClass.addContent(getInputFormElement("money", field)).addContent(EMPTY_LINE);
            return;
        }

        throw new IllegalStateException("Unknown and unsupported java type: " + type);
    }

    private FunctionDeclaration getSelectForAssociatedBeanFunction(final Column column) {
        importsManager.addImport("java.util.List");
        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("org.beanmaker.util.IdNamePair");

        final String field = column.getJavaName();
        final String associatedBeanClass = column.getAssociatedBeanClass();
        final String bundleKey = uncapitalize(SourceFiles.chopId(field));
        final String parametersClass = associatedBeanClass + "Parameters";
        final String parametersVar = uncapitalize(getVarNameForClass(associatedBeanClass)) + "Parameters";

        final FunctionDeclaration functionDeclaration = new FunctionDeclaration("compose" + capitalize(field) + "FormElement")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Tag", "form"));

        functionDeclaration.addContent(
                new VarDeclaration(parametersClass, parametersVar, new ObjectCreation(parametersClass)).markAsFinal()
        ).addContent(
                VarDeclaration.createListDeclaration("IdNamePair", "pairs").markAsFinal()
        ).addContent(
                new FunctionCall("add", "pairs").byItself()
                        .addArgument(new ObjectCreation("IdNamePair")
                                .addArgument(quickQuote("0"))
                                .addArgument(new FunctionCall("getString", "resourceBundle").addArgument(quickQuote(bundleKey + "_please_select"))))
        ).addContent(
                new FunctionCall("addAll", "pairs").byItself()
                        .addArgument(new FunctionCall("getIdNamePairs", associatedBeanClass)
                                .addArgument(new FunctionCall("getNamingFields", parametersVar))
                                .addArgument(new FunctionCall("getOrderingFields", parametersVar)))
        ).addContent(EMPTY_LINE).addContent(
                new FunctionCall("child", "form").byItself().addArgument(
                        new FunctionCall("getSelectField", "htmlFormHelper")
                                .addArgument(quickQuote(field))
                                .addArgument(getId())
                                .addArgument(addFieldValueArgument(field, false))
                                .addArgument(getLabelArgument(field))
                                .addArgument("pairs")
                                .addArgument(new FunctionCall("is" + capitalize(field) + "RequiredInHtmlForm"))
                )
        );

        return functionDeclaration;
    }

    private FunctionCall addFieldValueArgument(final String field, final boolean booleanField) {
        if (booleanField)
            return new FunctionCall("is" + capitalize(field), beanVarName);

        return new FunctionCall("get" + capitalize(field), beanVarName);
    }

    /*private FunctionDeclaration getInputFormElement(final String inputType, final String field) {
        importsManager.addImport("org.jcodegen.html.InputTag");

        final FunctionDeclaration functionDeclaration = new FunctionDeclaration("compose" + capitalize(field) + "FormElement").visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Tag", "form"));

        final String fieldVar;
        if (!inputType.equals("TEXT") && !inputType.equals("EMAIL"))
            fieldVar = field + "Str";
        else
            fieldVar = field;
        final String inputTypeVal;
        if (inputType.equals("money"))
            inputTypeVal = "TEXT";
        else
            inputTypeVal = inputType;

        functionDeclaration.addContent(
                new FunctionCall("child", "form").byItself().addArgument(
                        new FunctionCall("getTextField", "htmlFormHelper")
                                .addArgument(quickQuote(field))
                                .addArgument(getId())
                                .addArgument(addFieldValueArgument(fieldVar, false))
                                .addArgument(getLabelArgument(field))
                                .addArgument("InputTag.InputType." + inputTypeVal)
                                .addArgument(new FunctionCall("is" + capitalize(field) + "RequiredInHtmlForm"))
                )
        );

        return functionDeclaration;
    }*/

    private void addInputFormElement(final String inputType, final String field) {
        importsManager.addImport("org.jcodegen.html.InputTag");

        final String paramsFunctionName = "get" + capitalize(field) + "FormElementParameters";
        final FunctionDeclaration paramsFunctionDeclaration =
                new FunctionDeclaration(paramsFunctionName, "HFHParameters")
                        .visibility(Visibility.PROTECTED);

        final String fieldVar;
        if (!inputType.equals("TEXT") && !inputType.equals("EMAIL"))
            fieldVar = field + "Str";
        else
            fieldVar = field;
        final String inputTypeVal;
        if (inputType.equals("money"))
            inputTypeVal = "TEXT";
        else
            inputTypeVal = inputType;

        paramsFunctionDeclaration.addContent(
                new VarDeclaration("HFHParameters", "params", new ObjectCreation("HFHParameters"))
                        .markAsFinal());
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setField", quickQuote(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setIdBean", getId()));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall("setValue", addFieldValueArgument(fieldVar, false)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setFieldLabel", getLabelArgument(field)));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall("setInputType", "InputTag.InputType." + inputTypeVal));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall(
                        "setRequired",
                        new FunctionCall("is" + capitalize(field) + "RequiredInHtmlForm")));
        paramsFunctionDeclaration.addContent(new ReturnStatement("params"));

        final FunctionDeclaration getElementFunctionDeclaration =
                new FunctionDeclaration("compose" + capitalize(field) + "FormElement")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"));

        getElementFunctionDeclaration.addContent(
                new FunctionCall("child", "form").byItself().addArgument(
                        new FunctionCall("getTextField", "htmlFormHelper")
                                .addArgument(new FunctionCall(paramsFunctionName))
                )
        );

        javaClass
                .addContent(paramsFunctionDeclaration)
                .addContent(EMPTY_LINE)
                .addContent(getElementFunctionDeclaration)
                .addContent(EMPTY_LINE);
    }

    private FunctionCall getParamAdjunctionCall(final String paramSetterFunction, final String value) {
        return new FunctionCall(paramSetterFunction, "params").addArgument(value).byItself();
    }

    private FunctionCall getParamAdjunctionCall(final String paramSetterFunction, final Expression value) {
        return new FunctionCall(paramSetterFunction, "params").addArgument(value).byItself();
    }

    private FunctionDeclaration getTextAreaFormElement(final String field) {
        final FunctionDeclaration functionDeclaration =  new FunctionDeclaration("compose" + capitalize(field) + "FormElement").visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Tag", "form"));

        functionDeclaration.addContent(
                new FunctionCall("child", "form").byItself().addArgument(
                        new FunctionCall("getTextAreaField", "htmlFormHelper")
                                .addArgument(quickQuote(field))
                                .addArgument(getId())
                                .addArgument(addFieldValueArgument(field, false))
                                .addArgument(getLabelArgument(field))
                                .addArgument(new FunctionCall("is" + capitalize(field) + "RequiredInHtmlForm"))
                )
        );

        return functionDeclaration;
    }

    private FunctionDeclaration getCheckboxFormElement(final String field) {
        return new FunctionDeclaration("compose" + capitalize(field) + "FormElement")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Tag", "form"))
                .addContent(
                        new FunctionCall("child", "form").byItself().addArgument(
                                new FunctionCall("getCheckboxField", "htmlFormHelper")
                                        .addArgument(quickQuote(field))
                                        .addArgument(getId())
                                        .addArgument(addFieldValueArgument(field, true))
                                        .addArgument(getLabelArgument(field))
                        )
                );
    }

    private FunctionCall setterCall(final String field) {
        return new FunctionCall("set" + capitalize(field), beanVarName).byItself();
    }

    private void addAllFieldsSetter() {
        final FunctionDeclaration setAllFieldsFunction = new FunctionDeclaration("setAllFields")
                .addArgument(new FunctionArgument("ServletRequest", "request"))
                .addContent(new FunctionCall("reset").byItself())
                .addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();

                if (type.equals("boolean")) {
                    setAllFieldsFunction.addContent(
                            new IfBlock(new Condition(Comparison.isNotNull(getRequestParameterFunctionCall(field)))).addContent(
                                    setterCall(field).addArgument("true")
                            ).elseClause(new ElseBlock().addContent(
                                    setterCall(field).addArgument("false")
                            ))
                    );
                }

                else if (type.equals("int") || type.equals("long")) {
                    if (field.startsWith("id")) {
                        importsManager.addImport("org.dbbeans.util.Strings");
                        setAllFieldsFunction.addContent(
                                new VarDeclaration("String", field + "ParamStr",
                                        getRequestParameterFunctionCall(field)).markAsFinal()
                        ).addContent(
                                new IfBlock(new Condition(Comparison.isNotNull(field + "ParamStr"))).addContent(
                                        setterCall(field).addArgument(new FunctionCall("get" + capitalize(type) + "Val", "Strings").addArgument(field + "ParamStr"))
                                ).elseClause(new ElseBlock().addContent(
                                        setterCall(field).addArgument("0")
                                ))
                        );
                    } else {
                        setAllFieldsFunction.addContent(
                                setterCall(field + "Str").addArgument(getRequestParameterFunctionCall(field))
                        );
                    }
                }

                else if (JAVA_TEMPORAL_TYPES.contains(type)) {
                    setAllFieldsFunction.addContent(
                            setterCall(field + "Str").addArgument(getRequestParameterFunctionCall(field))
                    );
                }

                else if (type.equals("String")) {
                    setAllFieldsFunction.addContent(
                            setterCall(field).addArgument(getRequestParameterFunctionCall(field))
                    );
                }

                else if (type.equals("Money")) {
                    setAllFieldsFunction.addContent(
                            setterCall(field + "Str").addArgument(getRequestParameterFunctionCall(field))
                    );
                }

                else
                    throw new IllegalStateException("Apparently unsupported type " + type + " encountered.");
            }
        }

        setAllFieldsFunction.addContent(EMPTY_LINE).addContent(
                new IfBlock(new Condition("captchaControl")).addContent(
                        new Assignment("captchaValue", getRequestParameterFunctionCall("captchaValue"))
                ).addContent(
                        new IfBlock(new Condition(Comparison.isNull("captchaValue"))).addContent(
                                new Assignment("captchaValue", EMPTY_STRING)
                        )
                )
        );

        javaClass.addContent(setAllFieldsFunction).addContent(EMPTY_LINE);
    }

    private FunctionCall getRequestParameterFunctionCall(final String param) {
        return new FunctionCall("getParameter").addArguments("request", quickQuote(param));
    }

    private void addDataOKChecker() {
        final FunctionDeclaration dataOKFunction = new FunctionDeclaration("isDataOK", "boolean").addContent(
                new VarDeclaration("boolean", "ok", new FunctionCall("isDataOK", beanVarName))
        ).addContent(
                new FunctionCall("clear", "errorMessages").byItself()
        ).addContent(
                new FunctionCall("addAll", "errorMessages").addArgument(new FunctionCall("getErrorMessages", beanVarName)).byItself()
        ).addContent(EMPTY_LINE);

        dataOKFunction.addContent(
                new IfBlock(new Condition("captchaControl")).addContent(
                        new IfBlock(new Condition(new FunctionCall("equals", "captchaValue").addArgument("captchaControlValue"), true)).addContent(
                                new FunctionCall("add", "errorMessages").byItself()
                                        .addArgument(new ObjectCreation("ErrorMessage")
                                                .addArgument(getId())
                                                .addArgument(quickQuote("captchaValue"))
                                                .addArgument(new FunctionCall("getString", "resourceBundle").addArgument(quickQuote("captchaFieldName")))
                                                .addArgument(new FunctionCall("getInvalidCaptchaErrorMessage")))
                        ).addContent(new Assignment("ok", "false"))
                )
        ).addContent(EMPTY_LINE);

        dataOKFunction.addContent(
                new ReturnStatement("ok")
        );

        javaClass.addContent(dataOKFunction).addContent(EMPTY_LINE);
    }

    private void addUpdateDBFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("updateDB").addContent(
                        new FunctionCall("updateDB", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setUpdateDB").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("updateDB").byItself()
                )
        ).addContent(EMPTY_LINE);
    }

    private void addResetFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("reset").addContent(
                        new FunctionCall("reset", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("fullReset").addContent(
                        new FunctionCall("fullReset", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setReset").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("reset").byItself()
                )
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("setFullReset").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("fullReset").byItself()
                )
        ).addContent(EMPTY_LINE);
    }

    private void addDeleteFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("delete").addContent(
                        new FunctionCall("delete", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setDelete").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("delete").byItself()
                )
        );
    }

    private void addItemOrderManagement() {
        javaClass.addContent(
                new FunctionDeclaration("setItemOrderMoveUp").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("itemOrderMoveUp", beanVarName).byItself()
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setItemOrderMoveDown").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("itemOrderMoveDown", beanVarName).byItself()
                )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addViewPrelude();
        addIdFunctions();
        addChecksForRequiredFields();
        newLine();
        addHTMLFormGetter();
        newLine();
        addAllFieldsSetter();
        newLine();
        addDataOKChecker();
        addUpdateDBFunctions();
        addResetFunctions();
        addDeleteFunctions();
        if (columns.hasItemOrder()) {
            newLine();
            addItemOrderManagement();
        }
    }
}
