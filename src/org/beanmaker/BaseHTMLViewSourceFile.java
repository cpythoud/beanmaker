package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.Expression;
import org.jcodegen.java.ForLoop;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.beanmaker.SourceFiles.chopId;

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
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.ErrorMessage");

        importsManager.addImport("javax.servlet.ServletRequest");

        importsManager.addImport("org.beanmaker.util.HFHParameters");

        importsManager.addImport("org.jcodegen.html.FormTag");
        importsManager.addImport("org.jcodegen.html.Tag");

        importsManager.addImport("java.util.Locale");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("BaseHTMLView").implementsInterface("DbBeanHTMLViewInterface");
    }

    @Override
    protected void addConstructorWithBeanAndLanguage() {
        javaClass.addContent(
                javaClass.createConstructor()
                        .addArgument(new FunctionArgument(beanName, beanVarName))
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new FunctionCall("super")
                                        .addArgument(quickQuote(bundleName + "-HTML"))
                                        .byItself()
                        )
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
                                                        new FunctionCall("setLocale")
                                                                .byItself()
                                                                .addArgument(new FunctionCall("getLocale", "dbBeanLanguage"))
                                                ))
                        )
        );
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
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getHtmlFormFunction =
                new FunctionDeclaration("getHtmlFormTag", "FormTag")
                        .annotate("@Override")
                        .addContent(
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
                new FunctionDeclaration("getSubmitButtonParameters", "HFHParameters").visibility(Visibility.PROTECTED)
                        .addContent(getNewHFHParametersDeclaration())
                        .addContent(getParamAdjunctionCall("setBeanName", quickQuote(beanName)))
                        .addContent(getParamAdjunctionCall("setIdBean", getId()))
                        .addContent(getParamAdjunctionCall(
                                "setButtonLabel",
                                new FunctionCall("getString", "resourceBundle")
                                        .addArgument(quickQuote("submit_button"))))
                        .addContent(new ReturnStatement("params"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("composeSubmitButton").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("Tag", "form"))
                        .addContent(
                                new FunctionCall("child", "form").byItself()
                                        .addArgument(new FunctionCall("getSubmitButton", "htmlFormHelper")
                                                .addArgument(new FunctionCall("getSubmitButtonParameters")))
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

    private void addFieldHtmlFormFunctions(final Column column) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (type.equals("boolean")) {
            addCheckboxFormElement(field);
            return;
        }

        if (type.equals("int") || type.equals("long")) {
            if (field.startsWith("id") && column.hasAssociatedBean()) {
                if (column.isLabelReference())
                    addLanguageDependantFieldFunctions(column);
                else
                    addSelectForAssociatedBeanFunctions(column);
            } else
                addInputFormElement("NUMBER", field);
            return;
        }

        if (type.equals("String")) {
            if (column.getDisplaySize() < TEXTAREA_THRESHOLD) {
                if (field.equalsIgnoreCase("email") || field.equalsIgnoreCase("e-mail"))
                    addInputFormElement("EMAIL", field);
                else
                    addInputFormElement("TEXT", field);
            } else {
                addTextAreaFormElement(field);
            }
            return;
        }

        if (type.equals("Date")) {
            addInputFormElement("DATE", field);
            return;
        }

        if (type.equals("Time")) {
            addInputFormElement("TIME", field);
            return;
        }

        if (type.equals("Timestamp")) {
            addInputFormElement("DATETIME", field);
            return;
        }

        if (type.equals("Money")) {
            addInputFormElement("money", field);
            return;
        }

        throw new IllegalStateException("Unknown and unsupported java type: " + type);
    }

    private void addSelectForAssociatedBeanFunctions(final Column column) {
        importsManager.addImport("java.util.List");
        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("org.beanmaker.util.IdNamePair");

        final String field = column.getJavaName();
        final String associatedBeanClass = column.getAssociatedBeanClass();
        final String bundleKey = uncapitalize(chopId(field));
        final String parametersClass = associatedBeanClass + "Parameters";
        final String parametersVar = uncapitalize(getVarNameForClass(associatedBeanClass)) + "Parameters";

        final String paramsFunctionName = getParamsFunctionName(field);
        final FunctionDeclaration paramsFunctionDeclaration = getNewParamsFunctionDeclaration(paramsFunctionName);

        paramsFunctionDeclaration.addContent(
                new VarDeclaration(parametersClass, parametersVar, new ObjectCreation(parametersClass)).markAsFinal()
        ).addContent(
                VarDeclaration.createListDeclaration("IdNamePair", "pairs").markAsFinal()
        ).addContent(
                new FunctionCall("add", "pairs").byItself()
                        .addArgument(new ObjectCreation("IdNamePair")
                                .addArgument(quickQuote("0"))
                                .addArgument(new FunctionCall("getString", "resourceBundle")
                                        .addArgument(quickQuote(bundleKey + "_please_select"))))
        ).addContent(
                new FunctionCall("addAll", "pairs").byItself()
                        .addArgument(new FunctionCall("getIdNamePairs", associatedBeanClass)
                                .addArgument(new FunctionCall("getNamingFields", parametersVar))
                                .addArgument(new FunctionCall("getOrderingFields", parametersVar)))
        ).addContent(EMPTY_LINE);

        paramsFunctionDeclaration.addContent(getNewHFHParametersDeclaration());
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setField", quickQuote(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setIdBean", getId()));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall("setSelected", addFieldValueArgument(field, false)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setFieldLabel", getLabelArgument(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setSelectPairs", "pairs"));
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
                        new FunctionCall("getSelectField", "htmlFormHelper")
                                .addArgument(new FunctionCall(paramsFunctionName))
                )
        );

        javaClass
                .addContent(paramsFunctionDeclaration)
                .addContent(EMPTY_LINE)
                .addContent(getElementFunctionDeclaration)
                .addContent(EMPTY_LINE);
    }

    private FunctionCall addFieldValueArgument(final String field, final boolean booleanField) {
        if (booleanField)
            return new FunctionCall("is" + capitalize(field), beanVarName);

        return new FunctionCall("get" + capitalize(field), beanVarName);
    }

    private void addInputFormElement(final String inputType, final String field) {
        importsManager.addImport("org.jcodegen.html.InputTag");

        final String paramsFunctionName = getParamsFunctionName(field);
        final FunctionDeclaration paramsFunctionDeclaration = getNewParamsFunctionDeclaration(paramsFunctionName);

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

        paramsFunctionDeclaration.addContent(getNewHFHParametersDeclaration());
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

        final FunctionDeclaration getElementFunctionDeclaration = getNewElementFunctionDeclaration(field);

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

    private void addLanguageDependantFieldFunctions(final Column column) {
        final String field = column.getJavaName();

        final String paramsFunctionName = getParamsFunctionName(field);
        final FunctionDeclaration paramsFunctionDeclaration = getNewParamsFunctionDeclaration(paramsFunctionName);

        paramsFunctionDeclaration.addContent(getNewHFHParametersDeclaration());
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setField", quickQuote(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setIdBean", getId()));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setFieldLabel", getLabelArgument(field)));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall(
                        "setRequired",
                        new FunctionCall("is" + capitalize(field) + "RequiredInHtmlForm")));
        paramsFunctionDeclaration.addContent(new ReturnStatement("params"));

        final FunctionDeclaration getElementFunctionDeclaration = getNewElementFunctionDeclaration(field);

        getElementFunctionDeclaration.addContent(
                new ForLoop("DbBeanLanguage dbBeanLanguage: Labels.getAllActiveLanguages()").addContent(
                        new FunctionCall("child", "form")
                                .byItself()
                                .addArgument(
                                        new FunctionCall("getLabelFormField", "htmlFormHelper")
                                                .addArgument(new FunctionCall("get" + chopId(field), beanVarName)
                                                        .addArgument("dbBeanLanguage"))
                                                .addArgument("dbBeanLanguage")
                                                .addArgument(new FunctionCall("get" + capitalize(field) + "FormElementParameters"))
                                )
                )
        );

        javaClass
                .addContent(paramsFunctionDeclaration)
                .addContent(EMPTY_LINE)
                .addContent(getElementFunctionDeclaration)
                .addContent(EMPTY_LINE);
    }

    private String getParamsFunctionName(final String field) {
        return "get" + capitalize(field) + "FormElementParameters";
    }

    private FunctionCall getParamAdjunctionCall(final String paramSetterFunction, final String value) {
        return new FunctionCall(paramSetterFunction, "params").addArgument(value).byItself();
    }

    private FunctionCall getParamAdjunctionCall(final String paramSetterFunction, final Expression value) {
        return new FunctionCall(paramSetterFunction, "params").addArgument(value).byItself();
    }

    private FunctionDeclaration getNewParamsFunctionDeclaration(final String functionName) {
        return new FunctionDeclaration(functionName, "HFHParameters").visibility(Visibility.PROTECTED);
    }

    private VarDeclaration getNewHFHParametersDeclaration() {
        return new VarDeclaration("HFHParameters", "params", new ObjectCreation("HFHParameters"))
                .markAsFinal();
    }

    private FunctionDeclaration getNewElementFunctionDeclaration(final String field) {
        return new FunctionDeclaration("compose" + capitalize(field) + "FormElement")
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("Tag", "form"));
    }

    private void addTextAreaFormElement(final String field) {
        final String paramsFunctionName = getParamsFunctionName(field);
        final FunctionDeclaration paramsFunctionDeclaration = getNewParamsFunctionDeclaration(paramsFunctionName);

        paramsFunctionDeclaration.addContent(getNewHFHParametersDeclaration());
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setField", quickQuote(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setIdBean", getId()));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall("setValue", addFieldValueArgument(field, false)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setFieldLabel", getLabelArgument(field)));
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
                        new FunctionCall("getTextAreaField", "htmlFormHelper")
                                .addArgument(new FunctionCall(paramsFunctionName))
                )
        );

        javaClass
                .addContent(paramsFunctionDeclaration)
                .addContent(EMPTY_LINE)
                .addContent(getElementFunctionDeclaration)
                .addContent(EMPTY_LINE);
    }

    private void addCheckboxFormElement(final String field) {
        final String paramsFunctionName = getParamsFunctionName(field);
        final FunctionDeclaration paramsFunctionDeclaration = getNewParamsFunctionDeclaration(paramsFunctionName);

        paramsFunctionDeclaration.addContent(getNewHFHParametersDeclaration());
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setField", quickQuote(field)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setIdBean", getId()));
        paramsFunctionDeclaration.addContent(
                getParamAdjunctionCall("setChecked", addFieldValueArgument(field, true)));
        paramsFunctionDeclaration.addContent(getParamAdjunctionCall("setFieldLabel", getLabelArgument(field)));
        paramsFunctionDeclaration.addContent(new ReturnStatement("params"));

        final FunctionDeclaration getElementFunctionDeclaration = getNewElementFunctionDeclaration(field);

        getElementFunctionDeclaration.addContent(
                new FunctionCall("child", "form").byItself().addArgument(
                        new FunctionCall("getCheckboxField", "htmlFormHelper")
                                .addArgument(new FunctionCall(paramsFunctionName))
                )
        );

        javaClass
                .addContent(paramsFunctionDeclaration)
                .addContent(EMPTY_LINE)
                .addContent(getElementFunctionDeclaration)
                .addContent(EMPTY_LINE);
    }

    private FunctionCall setterCall(final String field) {
        return new FunctionCall("set" + capitalize(field), beanVarName).byItself();
    }

    private void addAllFieldsSetter() {
        final FunctionDeclaration setAllFieldsFunction = new FunctionDeclaration("setAllFields")
                .annotate("@Override")
                .addArgument(new FunctionArgument("ServletRequest", "request"))
                .addContent(new FunctionCall("reset").byItself())
                .addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.isLabelReference()) {
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

        if (columns.hasLabels()) {
            final ForLoop languageLoop =
                    new ForLoop("DbBeanLanguage dbBeanLanguage: Labels.getAllActiveLanguages()")
                            .addContent(
                                    new VarDeclaration(
                                            "String",
                                            "iso",
                                            new FunctionCall("getCapIso", "dbBeanLanguage"))
                                            .markAsFinal()
                            );

            for (Column column: columns.getList())
                if (column.isLabelReference()) {
                    final String choppedIdFieldName = chopId(column.getJavaName());
                    languageLoop.addContent(
                            new FunctionCall("set" + choppedIdFieldName, beanVarName)
                                    .addArgument("dbBeanLanguage")
                                    .addArgument(
                                            new FunctionCall("getParameter", "request")
                                                    .addArgument(quickQuote(uncapitalize(choppedIdFieldName))
                                                            + " + iso")
                                    )
                                    .byItself()
                    );
                }

            setAllFieldsFunction.addContent(EMPTY_LINE).addContent(languageLoop);
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
        final FunctionDeclaration dataOKFunction = new FunctionDeclaration("isDataOK", "boolean")
                .annotate("@Override")
                .addContent(
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
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setUpdateDB").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("updateDB").byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);
    }

    private void addResetFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("reset").addContent(
                        new FunctionCall("reset", beanVarName).byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("fullReset").addContent(
                        new FunctionCall("fullReset", beanVarName).byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setReset").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("reset").byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("setFullReset").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("fullReset").byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);
    }

    private void addDeleteFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("delete").addContent(
                        new FunctionCall("delete", beanVarName).byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("setDelete").addArgument(new FunctionArgument("String", "dummy")).addContent(
                        new FunctionCall("delete").byItself()
                ).annotate("@Override")
        ).addContent(EMPTY_LINE);
    }

    private void addSetLocaleFunction() {
        javaClass.addContent(
                new FunctionDeclaration("setLocale")
                        .annotate("@Override")
                        .addArgument(new FunctionArgument("Locale", "locale"))
                        .addContent(
                                new FunctionCall("setLocale", "super")
                                        .addArgument("locale")
                                        .byItself()
                        )
                        .addContent(
                                new FunctionCall("setLocale", beanVarName)
                                        .addArgument("locale")
                                        .byItself()
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
        ).addContent(EMPTY_LINE);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addViewPrelude();
        //addIdFunctions();
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
        addSetLocaleFunction();
        if (columns.hasItemOrder()) {
            newLine();
            addItemOrderManagement();
        }
    }
}
