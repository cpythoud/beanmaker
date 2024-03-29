package org.beanmaker;

import java.util.List;
import java.util.Set;

import org.jcodegen.java.AnonymousClassCreation;
import org.jcodegen.java.Assignment;
import org.jcodegen.java.CatchBlock;
import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ConstructorDeclaration;
import org.jcodegen.java.ElseBlock;
import org.jcodegen.java.ElseIfBlock;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.ForLoop;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.GenericType;
import org.jcodegen.java.IfBlock;
import org.jcodegen.java.JavaClass;
import org.jcodegen.java.JavaCodeBlock;
import org.jcodegen.java.Lambda;
import org.jcodegen.java.LambdaExpression;
import org.jcodegen.java.LineOfCode;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.OperatorExpression;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.TryBlock;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;
import org.jcodegen.java.WhileBlock;

import org.dbbeans.util.Strings;

import static org.beanmaker.SourceFiles.chopId;
import static org.dbbeans.util.Strings.*;

public class BaseClassSourceFile extends BeanCodeWithDBInfo {

    private final Set<String> types;

    private final String internalsVar;
    private final String parametersClass;
    private final String parametersVar;

    public BaseClassSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "Base", columns, tableName);

        internalsVar = beanVarName + "Internals";
        parametersClass = beanName + "Parameters";
        parametersVar = Strings.uncamelize(beanName).toUpperCase() + "_PARAMETERS";

        types = columns.getJavaTypes();

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("java.sql.PreparedStatement");
        importsManager.addImport("java.sql.ResultSet");
        importsManager.addImport("java.sql.SQLException");

        importsManager.addImport("java.util.ArrayList");
        importsManager.addImport("java.util.List");
        importsManager.addImport("java.util.Locale");

        importsManager.addImport("org.beanmaker.util.BeanInternals");
        if (columns.hasItemOrder()) {
            importsManager.addImport("org.beanmaker.util.DbBeanWithItemOrderInterface");
            if (columns.hasCodeField())
                importsManager.addImport("org.beanmaker.util.DbBeanWithCode");
        } else if (columns.hasCodeField())
            importsManager.addImport("org.beanmaker.util.DbBeanWithCode");
        else
            importsManager.addImport("org.beanmaker.util.DbBeanInterface");
        importsManager.addImport("org.beanmaker.util.DBQueries");
        importsManager.addImport("org.beanmaker.util.ErrorMessage");
        importsManager.addImport("org.beanmaker.util.IdNamePair");

        importsManager.addImport("org.dbbeans.sql.DBQueryRetrieveData");
        importsManager.addImport("org.dbbeans.sql.DBQuerySetup");
        //importsManager.addImport("org.dbbeans.sql.DBQuerySetupProcess");
        importsManager.addImport("org.dbbeans.sql.DBTransaction");
        importsManager.addImport("org.dbbeans.sql.SQLRuntimeException");
        importsManager.addImport("org.dbbeans.sql.queries.BooleanCheckQuery");

        importsManager.addImport("org.dbbeans.util.Strings");

        if (columns.containsNumericalData())
            importsManager.addImport("org.beanmaker.util.FormatCheckHelper");

        if (types.contains("Date")) {
            importsManager.addImport("java.sql.Date");
            importsManager.addImport("java.text.DateFormat");
            importsManager.addImport("org.dbbeans.util.Dates");
            importsManager.addImport("org.dbbeans.util.SimpleInputDateFormat");
        }
        if (types.contains("Time")) {
            importsManager.addImport("java.sql.Time");
            importsManager.addImport("java.text.DateFormat");
            importsManager.addImport("org.dbbeans.util.Dates");
            importsManager.addImport("org.dbbeans.util.SimpleInputTimeFormat");
        }
        if (types.contains("Timestamp")) {
            importsManager.addImport("java.sql.Timestamp");
            importsManager.addImport("java.text.DateFormat");
            importsManager.addImport("org.dbbeans.util.Dates");
            importsManager.addImport("org.dbbeans.util.SimpleInputDateFormat");
            importsManager.addImport("org.dbbeans.util.SimpleInputTimestampFormat");
        }
        if (types.contains("Money")) {
            importsManager.addImport("org.dbbeans.util.Money");
            importsManager.addImport("org.dbbeans.util.MoneyFormat");
        }

        if (columns.hasLastUpdate())
            importsManager.addImport("org.dbbeans.util.Dates");

        if (columns.hasLabels()) {
            importsManager.addImport("org.beanmaker.util.DbBeanLabel");
            importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
            importsManager.addImport("org.beanmaker.util.DbBeanRequiredLanguages");
            if (columns.hasLabelField())
                importsManager.addImport("org.beanmaker.util.DbBeanMultilingual");
        }

        if (columns.hasFiles())
            importsManager.addImport("org.beanmaker.util.DbBeanFile");

        importsManager.addImport("org.beanmaker.util.ToStringMaker");
    }

    private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("DbBean");
        if (columns.hasItemOrder()) {
            javaClass.implementsGenericInterface("DbBeanWithItemOrderInterface", beanName);
            if (columns.hasCodeField())
                javaClass.implementsInterface("DbBeanWithCode");
        } else if (columns.hasCodeField())
            javaClass.implementsInterface("DbBeanWithCode");
        else
            javaClass.implementsInterface("DbBeanInterface");

        if (columns.hasLabelField())
            javaClass.implementsInterface("DbBeanMultilingual");
    }

    private void addProperties() {
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_") || field.equals("id");
            final VarDeclaration declaration;

            if (type.equals("String"))
                declaration = new VarDeclaration("String", field, EMPTY_STRING);
            else if (type.equals("Money"))
                declaration = new VarDeclaration("Money", field, new ObjectCreation("Money")
                        .addArgument("0")
                        .addArgument(new FunctionCall("getDefaultMoneyFormat", parametersVar)));
            else
                declaration = new VarDeclaration(type, field);

            addProperty(declaration);

            if (type.equals("Money"))
                addProperty(new VarDeclaration(
                        "String",
                        field + "Str",
                        new FunctionCall("toString", field)));
            if (JAVA_TEMPORAL_TYPES.contains(type)
                    || ((type.equals("int")
                    || type.equals("long"))
                    && !isIdReference
                    && !field.equals("itemOrder")))
                addProperty(new VarDeclaration("String", field + "Str", EMPTY_STRING));
            if (isIdReference && column.getAssociatedBeanClass().equals("DbBeanLabel"))
                addProperty(new VarDeclaration("DbBeanLabel", uncapitalize(chopId(field))));
        }

        if (columns.hasLastUpdate())
            addProperty("boolean", "updateOK");

        if (columns.hasOneToManyRelationships()) {
            boolean first = true;
            for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
                if (!relationship.isListOnly()) {
                    if (first) {
                        newLine();
                        first = false;
                    }
                    addProperty(VarDeclaration.createListDeclaration(relationship.getBeanClass(), relationship.getJavaName()));
                }
        }

        newLine();
        javaClass.addContent(new VarDeclaration("String", "DATABASE_TABLE_NAME", quickQuote(tableName)).visibility(Visibility.PROTECTED).markAsStatic().markAsFinal());
        javaClass.addContent(new VarDeclaration("String", "DATABASE_FIELD_LIST", quickQuote(getStaticFieldList())).visibility(Visibility.PROTECTED).markAsStatic().markAsFinal());

        newLine();
        javaClass.addContent(
                new VarDeclaration("BeanInternals", internalsVar,
                        new ObjectCreation("BeanInternals").addArgument(quickQuote(bundleName))).markAsFinal().visibility(Visibility.PROTECTED)
        );
        final String parametersClass = beanName + "Parameters";
        javaClass.addContent(
                new VarDeclaration(parametersClass, parametersVar,
                        new ObjectCreation(parametersClass)).markAsFinal().markAsStatic().visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);

        if (columns.hasLabels()) {
            for (Column column: columns.getList())
                if (column.isLabelReference())
                    javaClass.addContent(
                            new VarDeclaration(
                                    "DbBeanRequiredLanguages",
                                    "requiredLanguagesFor" + chopId(column.getJavaName()),
                                    "null",
                                    Visibility.PROTECTED)
                    );

            newLine();
        }

        if (columns.hasExtraFields()) {
            for (ExtraField extraField: columns.getExtraFields()) {
                javaClass.addContent(new LineOfCode(extraField.toString()));
                if (extraField.requiresImport())
                    importsManager.addImport(extraField.getRequiredImport());
                if (extraField.requiresSecondaryImport())
                    importsManager.addImport(extraField.getSecondaryRequiredImport());
                if (extraField.requiresTernaryImport())
                    importsManager.addImport(extraField.getTernaryRequiredImport());
            }
            newLine();
        }
    }

    private String getStaticFieldList() {
        final StringBuilder list = new StringBuilder();

        for (Column column: columns.getList())
            list.append(tableName).append(".").append(column.getSqlName()).append(", ");
        list.delete(list.length() - 2, list.length());

        return list.toString();
    }

    private void addConstructors() {
        javaClass.addContent(getBaseConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getIdArgumentConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getCopyConstructor()).addContent(EMPTY_LINE);
        addProtectedConstructors();
    }

    private ConstructorDeclaration getBaseConstructor() {
        return javaClass.createConstructor();
    }

    private ConstructorDeclaration getIdArgumentConstructor() {
        return getBaseConstructor().addArgument(new FunctionArgument("long", "id")).addContent("setId(id);");
    }

    private ConstructorDeclaration getCopyConstructor() {
        final String modelVarName = beanVarName + "Model";

        final ConstructorDeclaration copyConstructor = getBaseConstructor();
        copyConstructor
                .addArgument(new FunctionArgument(beanName + "Base", modelVarName))
                .addContent(new Assignment("id", "0"));
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_");
            if (!field.equals("id")) {
                if (field.equals("itemOrder"))
                    copyConstructor.addContent(new Assignment("itemOrder", "0"));
                else if (isIdReference || type.equals("boolean") || type.equals("String"))
                    copyConstructor.addContent(new Assignment(field, modelVarName + "." + field));
                else
                    copyConstructor
                            .addContent(new FunctionCall("set" + capitalize(field))
                                    .addArgument(modelVarName + "." + field).byItself());
            }
        }

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly()) {
                final String beanClass = relationship.getBeanClass();
                final String beanObject = uncapitalize(beanClass);
                final String javaName = relationship.getJavaName();
                copyConstructor.addContent(EMPTY_LINE).addContent(
                        new ForLoop(beanClass + " " + beanObject + ": " + modelVarName + "." + javaName).addContent(
                                new FunctionCall("add", javaName).byItself().addArgument(
                                        new ObjectCreation(beanClass).addArgument(new FunctionCall("getId", beanObject))
                                )
                        )
                );
            }

        return copyConstructor;
    }

    private ConstructorDeclaration getCommonFieldConstructor() {
        final ConstructorDeclaration fieldConstructor = getBaseConstructor().visibility(Visibility.PROTECTED);
        for (Column column: columns.getList())
            fieldConstructor.addArgument(new FunctionArgument(column.getJavaType(), column.getJavaName()));

        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_") || field.equals("id");
            if (isIdReference || field.equals("itemOrder") || type.equals("boolean") || type.equals("String"))
                fieldConstructor.addContent(new Assignment("this." + field, field));
            else
                fieldConstructor.addContent(new FunctionCall("set" + capitalize(field)).addArgument(field).byItself());
        }

        return fieldConstructor;
    }

    private void addProtectedConstructors() {
        final ConstructorDeclaration fieldConstructor = getCommonFieldConstructor();

        boolean needExtraConstructors = false;
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly()) {
                if (!needExtraConstructors) {
                    fieldConstructor.addContent(EMPTY_LINE);
                    needExtraConstructors = true;
                }
                final String javaName = relationship.getJavaName();
                fieldConstructor.addContent(
                        new Assignment(
                                javaName,
                                new FunctionCall("initialized" + capitalize(javaName)).addArgument("id"))
                );
            }

        javaClass.addContent(fieldConstructor).addContent(EMPTY_LINE);

        if (needExtraConstructors) {
            final ConstructorDeclaration extraFieldConstructor = getCommonFieldConstructor();

            for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
                if (!relationship.isListOnly()) {
                    final String beanClass = relationship.getBeanClass();
                    final String beanObject = uncapitalize(beanClass);
                    final String javaName = relationship.getJavaName();
                    extraFieldConstructor.addArgument(
                            new FunctionArgument(new GenericType("List", beanClass).toString(), javaName));
                    extraFieldConstructor.addContent(EMPTY_LINE).addContent(
                            new ForLoop(beanClass + " " + beanObject + ": " + javaName).addContent(
                                    new FunctionCall("add", "this." + javaName).byItself().addArgument(beanObject)
                            )
                    );
                }

            javaClass.addContent(extraFieldConstructor).addContent(EMPTY_LINE);
        }

        javaClass
                .addContent(getCommonResultSetConstructor()
                        .addContent(getCommonResultSetConstructorThisCall()))
                .addContent(EMPTY_LINE);

        if (needExtraConstructors) {
            final ConstructorDeclaration extraRsConstructor = getCommonResultSetConstructor();

            for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
                if (!relationship.isListOnly())
                    extraRsConstructor.addArgument(
                            new FunctionArgument(
                                    new GenericType(
                                            "List",
                                            relationship.getBeanClass()).toString(),
                                    relationship.getJavaName()));

            final FunctionCall thisCall = getCommonResultSetConstructorThisCall();

            for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
                if (!relationship.isListOnly())
                    thisCall.addArgument(relationship.getJavaName());

            javaClass.addContent(extraRsConstructor.addContent(thisCall)).addContent(EMPTY_LINE);
        }
    }

    private ConstructorDeclaration getCommonResultSetConstructor() {
        return getBaseConstructor()
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("ResultSet", "rs"))
                .addException("SQLException");
    }

    private FunctionCall getCommonResultSetConstructorThisCall() {
        final FunctionCall thisCall = new FunctionCall("this").byItself();
        int index = 0;
        for (Column column: columns.getList()) {
            if (column.getJavaType().equals("Money"))
                thisCall.addArgument(new ObjectCreation("Money")
                        .addArgument(new FunctionCall("getLong", "rs").addArgument(Integer.toString(++index))));
            else
                thisCall.addArgument(new FunctionCall("get" + capitalize(column.getJavaType()), "rs").addArgument(Integer.toString(++index)));
        }

        return thisCall;
    }

    private void addSetIdFunction() {
        final List<OneToManyRelationship> relationships = columns.getOneToManyRelationships();

        final FunctionDeclaration function = new FunctionDeclaration("setId")
                .visibility(Visibility.PUBLIC)
                .addArgument(new FunctionArgument("long", "id")).annotate("@Override");

        // * check for bad ID
        function.addContent(new IfBlock(new Condition("id <= 0")).addContent("throw new IllegalArgumentException(\"id = \" + id + \" <= 0\");"))
                .addContent(EMPTY_LINE);

        // ! new lambda ResultSet processing part
        var lambdaIfBlock = new IfBlock(new Condition(new FunctionCall("next", "rs")));
        lambdaIfBlock.addContent(new Assignment("this.id", "id"));
        int index = 0;
        for (Column column : columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_");
            if (!field.equals("id")) {
                ++index;
                if (type.equals("Money")) {
                    lambdaIfBlock.addContent(new Assignment(field, new ObjectCreation("Money")
                            .addArgument(new FunctionCall("getLong", "rs").addArgument(Integer.toString(index)))
                            .addArgument(new FunctionCall("getDefaultMoneyFormat", parametersVar))));
                    lambdaIfBlock.addContent(
                            new Assignment(field + "Str",
                                    new FunctionCall("toString", field))
                    );
                } else {
                    final String getterName = "get" + capitalize(type);
                    lambdaIfBlock.addContent(new Assignment(field, new FunctionCall(getterName, "rs")
                            .addArgument(Integer.toString(index))));
                    if (JAVA_TEMPORAL_TYPES.contains(type))
                        lambdaIfBlock.addContent(
                                new Assignment(field + "Str",
                                        new FunctionCall("convert" + type + "ToString").addArgument(field))
                        );
                    if ((type.equals("int") || type.equals("long")) && !field.equals("itemOrder") && !isIdReference)
                        lambdaIfBlock.addContent(
                                new Assignment(field + "Str",
                                        new FunctionCall("valueOf", "String").addArgument(field))
                        );
                }
            }
        }
        lambdaIfBlock.elseClause(new ElseBlock().addContent(
                new ExceptionThrow("IllegalArgumentException").addArgument("\"id = \" + id + \" does not exist\"")));

        // ! new lambda to initialize data
        function.addContent(
                new FunctionCall("processQuery", "dbAccess")
                        .byItself()
                        .addArgument(quickQuote(getReadSQLQuery()))
                        .addArgument(new LambdaExpression()
                                .addLambdaParameter("stat")
                                .addContent(new FunctionCall("setLong", "stat").addArguments("1", "id")))
                        .addArgument(new Lambda()
                                .addLambdaParameter("rs")
                                .addContent(lambdaIfBlock))
        ).addContent(EMPTY_LINE);

        // * extra DB actions
        function.addContent("initExtraDbActions(id);");

        for (OneToManyRelationship relationship : relationships)
            if (!relationship.isListOnly())
                function.addContent(
                        new Assignment(
                                relationship.getJavaName(),
                                new FunctionCall("initialized" + capitalize(relationship.getJavaName()))
                                        .addArgument("id")
                        ));

        // Nullification of labels
        if (columns.hasLabels()) {
            function.addContent(EMPTY_LINE);
            for (Column column : columns.getList())
                if (column.isLabelReference())
                    function.addContent(new Assignment(uncapitalize(chopId(column.getJavaName())), "null"));
        }

        function.addContent(EMPTY_LINE).addContent("postInitActions();");
        javaClass.addContent(function).addContent(EMPTY_LINE);


        // for objects containing one or more list of other kind of objects
        for (OneToManyRelationship relationship : relationships)
            if (!relationship.isListOnly()) {
                final FunctionDeclaration initializedBeansFunction =
                        new FunctionDeclaration(
                                "initialized" + capitalize(relationship.getJavaName()),
                                new GenericType("List", relationship.getBeanClass()))
                                .addArgument(new FunctionArgument("long", "id"))
                                .visibility(Visibility.PROTECTED);

                final JavaClass extraDBInnerClass = new JavaClass("DataFromDBQuery" + capitalize(relationship.getJavaName()))
                        .visibility(Visibility.NONE).implementsInterface("DBQuerySetupProcess")
                        .addContent(VarDeclaration.createListDeclaration(
                                relationship.getBeanClass(),
                                relationship.getJavaName()).markAsFinal())
                        .addContent(EMPTY_LINE)
                        .addContent(getInnerClassSetupPSWithIdFunction())
                        .addContent(EMPTY_LINE);
                final FunctionDeclaration processRSExtra = getInnerClassProcessRSFunctionStart()
                        .addContent(new WhileBlock(new Condition("rs.next()"))
                                .addContent(new FunctionCall("add", relationship.getJavaName())
                                        .byItself()
                                        .addArgument(new ObjectCreation(relationship.getBeanClass())
                                                .addArgument("rs"))));
                extraDBInnerClass.addContent(processRSExtra);

                initializedBeansFunction
                        .addContent(extraDBInnerClass)
                        .addContent(EMPTY_LINE);

                final String beanClass = relationship.getBeanClass();
                final String cappedJavaName = capitalize(relationship.getJavaName());
                final String parameterFieldName = uncapitalize(beanClass) + "Parameters";
                initializedBeansFunction.addContent(
                        new VarDeclaration(
                                beanClass + "Parameters",
                                parameterFieldName,
                                new ObjectCreation(beanClass + "Parameters")).markAsFinal()
                ).addContent(
                        new VarDeclaration(
                                "DataFromDBQuery" + cappedJavaName,
                                "dataFromDBQuery" + cappedJavaName,
                                new ObjectCreation("DataFromDBQuery" + cappedJavaName)).markAsFinal()
                ).addContent(
                        new FunctionCall("processQuery", "dbAccess")
                                .byItself()
                                .addArgument(getReadSQLQueryOneToManyRelationship(relationship, parameterFieldName))
                                .addArgument("dataFromDBQuery" + cappedJavaName)
                ).addContent(EMPTY_LINE);


                initializedBeansFunction
                        .addContent(
                                new ReturnStatement(
                                        "dataFromDBQuery"
                                                + capitalize(relationship.getJavaName())
                                                + "."
                                                + relationship.getJavaName()));

                this.javaClass.addContent(initializedBeansFunction).addContent(EMPTY_LINE);
            }

        javaClass.addContent(
                new FunctionDeclaration("initExtraDbActions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("long", "id"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postInitActions")
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("resetId").annotate("@Override")
                        .addContent("id = 0;")
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("refreshFromDataBase").addContent(
                        new IfBlock(new Condition("id == 0")).addContent(
                                new ExceptionThrow("IllegalArgumentException")
                                        .addArgument(quickQuote("Cannot refresh bean not yet commited to database"))
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionCall("setId")
                                .addArgument("id")
                                .byItself()
                )
        ).addContent(EMPTY_LINE);

        for (OneToManyRelationship relationship : relationships)
            if (!relationship.isListOnly()) {
                javaClass.addContent(
                        new FunctionDeclaration("refresh" + relationship.getBeanClass() + "ListFromDataBase").addContent(
                                new IfBlock(new Condition("id == 0")).addContent(
                                        new ExceptionThrow("IllegalArgumentException")
                                                .addArgument(quickQuote("Cannot refresh list in bean not yet commited to database"))
                                )
                        ).addContent(EMPTY_LINE).addContent(
                                new VarDeclaration(
                                        VarDeclaration.getParametrizedType("List", relationship.getBeanClass()),
                                        "refreshedList",
                                        new FunctionCall("initialized" + capitalize(relationship.getJavaName()))
                                                .addArgument("id")).markAsFinal()
                        ).addContent(
                                new FunctionCall("clear", relationship.getJavaName()).byItself()
                        ).addContent(
                                new FunctionCall("addAll", relationship.getJavaName()).byItself()
                                        .addArgument("refreshedList")
                        )
                ).addContent(EMPTY_LINE);
            }
    }

    private FunctionDeclaration getInnerClassSetupPSWithIdFunction() {
        return new FunctionDeclaration("setupPreparedStatement")
                .addException("SQLException")
                .annotate("@Override")
                .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                .addContent("stat.setLong(1, id);");
    }

    private FunctionDeclaration getInnerClassProcessRSFunctionStart() {
        return new FunctionDeclaration("processResultSet")
                .addException("SQLException")
                .annotate("@Override")
                .addArgument(new FunctionArgument("ResultSet", "rs"));
    }

    private void addEquals() {
        javaClass.addContent(
                new FunctionDeclaration("equals", "boolean").addArgument(new FunctionArgument("Object", "object")).annotate("@Override").addContent(
                        new IfBlock(new Condition(new Comparison("id", "0"))).addContent(
                                new ReturnStatement("false")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new IfBlock(new Condition("object instanceof " + beanName)).addContent(
                                new ReturnStatement("((" + beanName + ") object).getId() == id")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("false")
                )
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("hashCode", "int").annotate("@Override").addContent(
                        new IfBlock(new Condition(new Comparison("id", "0"))).addContent(
                                new ReturnStatement("-1")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("31 * ((int) (id ^ (id >>> 32))) + 17")
                )
        ).addContent(EMPTY_LINE);
    }

    private void addToString() {
        final FunctionDeclaration toStringFunction =
                new FunctionDeclaration("toString", "String")
                        .annotate("@Override")
                        .addContent(
                                new VarDeclaration(
                                        "ToStringMaker",
                                        "stringMaker",
                                        new ObjectCreation("ToStringMaker").addArgument("this")
                                ).markAsFinal()
                        );

        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_");
            if (!field.equals("id")) {
                toStringFunction.addContent(
                        new FunctionCall("addField", "stringMaker")
                                .addArguments(quickQuote(field), field)
                                .byItself()
                );
                if (!column.isSpecial() &&
                        (JAVA_TEMPORAL_TYPES.contains(type)
                                || type.equals("int")
                                || (type.equals("long") && !isIdReference)
                                || type.equals("Money")))
                {
                    toStringFunction.addContent(
                            new FunctionCall("addField", "stringMaker")
                                    .addArguments(quickQuote(field + "Str"), field + "Str")
                                    .byItself()
                    );
                }
            }
        }

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                toStringFunction.addContent(
                        new FunctionCall("addField", "stringMaker")
                                .addArguments(quickQuote(relationship.getJavaName()), relationship.getJavaName())
                                .byItself()
                );

        toStringFunction.addContent(
                new ReturnStatement(
                        new FunctionCall("toString", "stringMaker")
                )
        );

        javaClass.addContent(toStringFunction).addContent(EMPTY_LINE);
    }

    private void addSetters() {
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_");
            if (!field.equals("id") && !field.equals("lastUpdate") && !field.equals("modifiedBy") && !field.equals("itemOrder")) {
                final FunctionDeclaration setter = new FunctionDeclaration("set" + capitalize(field))
                        .addArgument(new FunctionArgument(type, field));
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    setter.addContent(
                            new IfBlock(new Condition(new Comparison(field, "null"))).addContent(
                                    new Assignment("this." + field, "null")
                            ).elseClause(new ElseBlock().addContent(
                                    new Assignment("this." + field,
                                            new ObjectCreation(type)
                                                    .addArgument(new FunctionCall("getTime", field)))
                            ))
                    );
                else
                    setter.addContent(
                            new Assignment("this." + field, field)
                    );
                if (type.equals("int") && !isIdReference)
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", "Integer")
                                    .addArgument(field))
                    );
                if (type.equals("long") && !isIdReference)
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", "Long")
                                    .addArgument(field))
                    );
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("convert" + capitalize(type) + "ToString")
                                    .addArgument(field))
                    );
                if (type.equals("Money"))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", field))
                    );
                if (column.isLabelReference())
                    setter.addContent(
                            new FunctionCall("init" + chopId(field)).byItself()
                    );
                javaClass.addContent(setter).addContent(EMPTY_LINE);

                if (column.couldHaveAssociatedBean() && column.hasAssociatedBean()) {
                    final String associatedBeanClass = column.getAssociatedBeanClass();
                    final String associatedBeanObject = uncapitalize(chopId(field));
                    final FunctionDeclaration fromObjectSetter =
                            new FunctionDeclaration("set" + capitalize(associatedBeanObject))
                                    .addArgument(new FunctionArgument(associatedBeanClass, associatedBeanObject))
                                    .addContent(
                                            new IfBlock(
                                                    new Condition(
                                                            new Comparison(
                                                                    new FunctionCall(
                                                                            "getId",
                                                                            associatedBeanObject)
                                                                    , "0")))
                                                    .addContent(new ExceptionThrow("IllegalArgumentException")
                                                            .addArgument(quickQuote("Cannot accept uninitialized "
                                                                    + associatedBeanClass + " bean (id = 0) as argument."))))
                                    .addContent(EMPTY_LINE)
                                    .addContent(new Assignment(
                                            field,
                                            new FunctionCall("getId", associatedBeanObject)));

                    if (column.isLabelReference())
                        fromObjectSetter.addContent(
                                new FunctionCall("init" + capitalize(associatedBeanObject)).byItself()
                        );

                    javaClass.addContent(fromObjectSetter).addContent(EMPTY_LINE);
                }

                if (JAVA_TEMPORAL_TYPES.contains(type)
                        || type.equals("Money")
                        || ((type.equals("int")
                        || type.equals("long"))
                        && !isIdReference))
                {
                    final FunctionDeclaration strSetter = new FunctionDeclaration("set" + capitalize(field) + "Str")
                            .addArgument(new FunctionArgument("String", field + "Str"))
                            .addContent(new Assignment("this." + field + "Str", field + "Str"));
                    javaClass.addContent(strSetter).addContent(EMPTY_LINE);
                }
            }
        }
    }

    private void addGetters() {
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final boolean isIdReference = column.getSqlName().startsWith("id_") || field.equals("id");

            final String prefix;
            if (type.equals("boolean"))
                prefix = "is";
            else
                prefix = "get";
            final FunctionDeclaration getter = new FunctionDeclaration(prefix + capitalize(field), type);

            if (field.equals("id") || field.equals("itemOrder") || field.equals("idLabel"))
                getter.annotate("@Override");

            if (JAVA_TEMPORAL_TYPES.contains(type))
                getter.addContent(
                        new IfBlock(new Condition(new Comparison(field, "null")))
                                .addContent(new ReturnStatement("null"))
                ).addContent(
                        EMPTY_LINE
                ).addContent(
                        new ReturnStatement(
                                new ObjectCreation(type).addArgument(new FunctionCall("getTime", field)))
                );
            else
                getter.addContent(
                        new ReturnStatement(field)
                );

            javaClass.addContent(getter).addContent(EMPTY_LINE);


            if (column.hasAssociatedBean()) {
                final String associatedBeanClass = column.getAssociatedBeanClass();
                final String associatedBeanGetterFunctionName = "get" + chopId(field);
                final FunctionDeclaration associatedBeanGetter =
                        new FunctionDeclaration(associatedBeanGetterFunctionName, associatedBeanClass);
                if (field.equals("idLabel"))
                    associatedBeanGetter.annotate("@Override");

                if (column.isLabelReference()) {
                    associatedBeanGetter.addContent(
                            new VarDeclaration(
                                    "DbBeanLabel",
                                    "dbBeanLabel",
                                    new FunctionCall("get", "Labels").addArgument(field)
                            ).markAsFinal()
                    ).addContent(EMPTY_LINE).addContent(
                            new IfBlock(new Condition(new Comparison(uncapitalize(chopId(field)), "null")))
                                    .addContent(new ReturnStatement("dbBeanLabel"))
                    ).addContent(EMPTY_LINE).addContent(
                            new ReturnStatement(
                                    new FunctionCall("replaceData", "Labels")
                                            .addArguments("dbBeanLabel", uncapitalize(chopId(field))))
                    );
                } else {
                    final ReturnStatement returnStatement;
                    if (column.isFileReference())
                        returnStatement =
                                new ReturnStatement(new FunctionCall("get", "LocalFiles").addArgument(field));
                    else
                        returnStatement =
                                new ReturnStatement(new ObjectCreation(associatedBeanClass).addArgument(field));

                    associatedBeanGetter.addContent(returnStatement);
                }

                javaClass.addContent(associatedBeanGetter).addContent(EMPTY_LINE);

                if (column.isLabelReference()) {
                    final FunctionDeclaration labelGetter =
                            new FunctionDeclaration(associatedBeanGetterFunctionName, "String")
                                    .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                                    .addContent(new FunctionCall("init" + chopId(field)).byItself())
                                    .addContent(
                                            new ReturnStatement(
                                                    new FunctionCall("get", uncapitalize(chopId(field)))
                                                            .addArgument("dbBeanLanguage")
                                            )
                                    );

                    if (field.equals("idLabel"))
                        labelGetter.annotate("@Override");

                    javaClass.addContent(labelGetter).addContent(EMPTY_LINE);
                }

            }

            if (JAVA_TEMPORAL_TYPES.contains(type)
                    || type.equals("Money")
                    || ((type.equals("int")
                    || type.equals("long"))
                    && !isIdReference
                    && !field.equals("itemOrder")))
            {
                final FunctionDeclaration strGetter =
                        new FunctionDeclaration("get" + capitalize(field) + "Str", "String")
                                .addContent(new ReturnStatement(field + "Str"));
                javaClass.addContent(strGetter).addContent(EMPTY_LINE);
            }

            if (JAVA_TEMPORAL_TYPES.contains(type)) {
                final String capField = capitalize(field);
                final FunctionDeclaration formattedGetter = new FunctionDeclaration("get" + capField + "Formatted", "String")
                        .addContent(new IfBlock(new Condition(new FunctionCall("is" + capField + "Empty")))
                                .addContent(new IfBlock(new Condition(new FunctionCall("is" + capField + "Required")))
                                        .addContent(getCannotDisplayBadDataException()))
                                .addContent(new ReturnStatement(EMPTY_STRING)))
                        .addContent(EMPTY_LINE)
                        .addContent(new IfBlock(new Condition(new FunctionCall("is" + capField + "OK"), true)).addContent(getCannotDisplayBadDataException()))
                        .addContent(EMPTY_LINE)
                        .addContent(new ReturnStatement(new FunctionCall("format" + type).addArgument(new FunctionCall("convertStringTo" + type).addArgument(field + "Str"))));
                javaClass.addContent(formattedGetter).addContent(EMPTY_LINE);
            }

            if (type.equals("boolean")) {
                final FunctionDeclaration booleanValGetter = new FunctionDeclaration("get" + capitalize(field) + "Val", "String")
                        .addContent(new IfBlock(new Condition(field))
                                .addContent(new ReturnStatement(new FunctionCall("getLabel", internalsVar).addArgument(quickQuote("true_value")))))
                        .addContent(EMPTY_LINE)
                        .addContent(new ReturnStatement(new FunctionCall("getLabel", internalsVar).addArgument(quickQuote("false_value"))));
                javaClass.addContent(booleanValGetter).addContent(EMPTY_LINE);
            }
        }
    }

    private void addLabelGetters() {
        for (Column column: columns.getList()) {
            final String field = column.getJavaName();
            if (!column.isSpecial()) {
                final FunctionDeclaration labelGetter =
                        new FunctionDeclaration("get" + capitalize(field) + "Label", "String")
                                .addContent(
                                        new ReturnStatement(
                                                new FunctionCall("getLabel", internalsVar)
                                                        .addArgument(quickQuote(field)
                                                        )
                                        )
                                );
                if (column.getJavaName().equals("idLabel"))
                    labelGetter.annotate("@Override");

                javaClass.addContent(labelGetter).addContent(EMPTY_LINE);
            }
        }
    }

    private ExceptionThrow getCannotDisplayBadDataException() {
        return new ExceptionThrow("IllegalArgumentException").addArgument(quickQuote("Cannot display bad data"));
    }

    private void addRequiredIndicators() {
        for (Column column: columns.getList()) {
            final String field = column.getJavaName();
            addIndicator(field,"Required", column.isRequired(), false);

            if (column.isLabelReference()) {
                final String requiredLanguageField = "requiredLanguagesFor" + chopId(field);
                javaClass.addContent(
                        new FunctionDeclaration("is" + capitalize(field) + "Required", "boolean")
                                .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                                .addContent(
                                        new IfBlock(new Condition(requiredLanguageField + " != null"))
                                                .addContent(
                                                        new ReturnStatement(
                                                                new FunctionCall(
                                                                        "isRequired",
                                                                        requiredLanguageField
                                                                ).addArgument("dbBeanLanguage")
                                                        )
                                                )
                                )
                                .addContent(EMPTY_LINE)
                                .addContent(
                                        new ReturnStatement(
                                                new FunctionCall(
                                                        "isRequired",
                                                        parametersVar + "." + requiredLanguageField
                                                ).addArgument("dbBeanLanguage")
                                        )
                                )
                ).addContent(EMPTY_LINE);
            }
        }
    }

    private void addUniqueIndicators() {
        for (Column column: columns.getList())
            addIndicator(column.getJavaName(), "ToBeUnique", column.isUnique(), false);
    }

    private void addOneToManyRelationshipManagement() {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            final String beanClass = relationship.getBeanClass();
            final String itemName = uncapitalize(beanClass);
            final String listName = relationship.getJavaName();

            if (relationship.isListOnly()) {
                importsManager.addImport("org.dbbeans.sql.DBQuerySetupRetrieveData");

                javaClass.addContent(
                        new FunctionDeclaration("get" + capitalize(listName), new GenericType("List", beanClass))
                                .addContent(
                                        new ReturnStatement(
                                                new FunctionCall("processQuery", "dbAccess")
                                                        .addArgument(
                                                                new OperatorExpression(quickQuote("SELECT ") + " + " + beanClass + ".DATABASE_FIELD_LIST + " + quickQuote(" FROM " + relationship.getTable() + " WHERE " + relationship.getIdSqlName() + "=? ORDER BY "),
                                                                        new FunctionCall("getOrderByFields", beanClass + "." + Strings.uncamelize(beanClass).toUpperCase() + "_PARAMETERS"),
                                                                        OperatorExpression.Operator.ADD)
                                                        )
                                                        .addArgument(
                                                                new LambdaExpression()
                                                                        .addLambdaParameter("stat")
                                                                        .addContent(new FunctionCall("setLong", "stat").addArguments("1", "id"))
                                                        )
                                                        .addArgument(beanClass + "::getList")
                                        )
                                )
                ).addContent(EMPTY_LINE);

                javaClass.addContent(
                        new FunctionDeclaration("getCountFor" + capitalize(listName), "long")
                                .addContent(
                                        new ReturnStatement(
                                                new FunctionCall("processQuery", "dbAccess")
                                                        .addArgument(quickQuote("SELECT COUNT(id) FROM " + relationship.getTable() + " WHERE " + relationship.getIdSqlName() + "=?"))
                                                        .addArgument(
                                                                new LambdaExpression()
                                                                        .addLambdaParameter("stat")
                                                                        .addContent(new FunctionCall("setLong", "stat").addArguments("1", "id"))
                                                        )
                                                        .addArgument(
                                                                new Lambda()
                                                                        .addLambdaParameter("rs")
                                                                        .addContent(new FunctionCall("next", "rs").byItself())
                                                                        .addContent(new ReturnStatement(new FunctionCall("getLong", "rs").addArgument("1")))
                                                        )
                                        )
                                )
                ).addContent(EMPTY_LINE);
            } else {
                importsManager.addImport("java.util.Collections");

                javaClass.addContent(
                        new FunctionDeclaration("add" + beanClass)
                                .addArgument(new FunctionArgument(beanClass, itemName))
                                .addContent(new FunctionCall("add", listName).byItself()
                                        .addArgument(itemName))
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("get" + beanClass, beanClass)
                                .addArgument(new FunctionArgument("int", "index"))
                                .addContent(getOneToManyRelationshipIndexOutOfBoundTest(listName))
                                .addContent(EMPTY_LINE)
                                .addContent(new ReturnStatement(new FunctionCall("get", listName).addArgument("index")))
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("insert" + beanClass)
                                .addArgument(new FunctionArgument("int", "index"))
                                .addArgument(new FunctionArgument(beanClass, itemName))
                                .addContent(getOneToManyRelationshipIndexOutOfBoundTest(listName))
                                .addContent(EMPTY_LINE)
                                .addContent(new FunctionCall("add", listName).addArgument("index").addArgument(itemName).byItself())
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("delete" + beanClass)
                                .addArgument(new FunctionArgument("int", "index"))
                                .addContent(getOneToManyRelationshipIndexOutOfBoundTest(listName))
                                .addContent(EMPTY_LINE)
                                .addContent(new FunctionCall("remove", listName).addArgument("index").byItself())
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("clear" + capitalize(listName))
                                .addContent(new FunctionCall("clear", listName).byItself())
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("get" + capitalize(listName), new GenericType("List", beanClass))
                                .addContent(new ReturnStatement(new FunctionCall("unmodifiableList", "Collections").addArgument(listName)))
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionDeclaration("getCountFor" + capitalize(listName), "long")
                                .addContent(new ReturnStatement(new FunctionCall("size", listName)))
                ).addContent(EMPTY_LINE);
            }
        }
    }

    private IfBlock getOneToManyRelationshipIndexOutOfBoundTest(final String listName) {
        return new IfBlock(new Condition(new Comparison("index", "0", Comparison.Comparator.LESS_THAN))
                .orCondition(new Condition(new Comparison("index", new FunctionCall("size", listName), Comparison.Comparator.GT_EQUAL))))
                .addContent(new ExceptionThrow("IndexOutOfBoundsException")
                        .addArgument(getOneToManyRelationshipIndexOutOfBoundExceptionText(listName)));
    }

    private String getOneToManyRelationshipIndexOutOfBoundExceptionText(final String listName) {
        return "\"Bounds : 0-\" + " + listName + ".size() + \", index : \" + index";
    }

    private void addItemOrderManagement() {
        if (!columns.hasItemOrder())
            return;

        final Column itemOrderField = columns.getItemOrderField();

        javaClass.addContent(
                new FunctionDeclaration("isFirstItemOrder", "boolean").annotate("@Override").addContent(
                        checkForItemOrderOperationOnUninitializedBean()
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("itemOrder == 1")
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration isLastItemOrderFunction =
                new FunctionDeclaration("isLastItemOrder", "boolean")
                        .annotate("@Override")
                        .addContent(
                                checkForItemOrderOperationOnUninitializedBean()
                        ).addContent(EMPTY_LINE);

        if (!itemOrderField.isUnique())
            isLastItemOrderFunction.addContent(
                    new IfBlock(new Condition(new Comparison(uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField())), "0"))).addContent(
                            new ReturnStatement(new Comparison("itemOrder", getMaxItemOrderFunctionCall(itemOrderField, false, true)))
                    ).addContent(EMPTY_LINE)
            );

        isLastItemOrderFunction.addContent(
                new ReturnStatement(new Comparison("itemOrder", getMaxItemOrderFunctionCall(itemOrderField, false, false)))
        );

        javaClass.addContent(isLastItemOrderFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("itemOrderMoveUp").annotate("@Override").addContent(
                        checkForItemOrderOperationOnUninitializedBean()
                ).addContent(EMPTY_LINE).addContent(
                        new IfBlock(new Condition(new FunctionCall("isFirstItemOrder"))).addContent(
                                new ExceptionThrow("IllegalArgumentException").addArgument(quickQuote("Cannot move Item Order above position 1 which it currently occupies"))
                        )
                ).addContent(EMPTY_LINE).addContent(
                        getItemOrderFunctionCalls("itemOrderMoveUp", itemOrderField)
                ).addContent(EMPTY_LINE).addContent(
                        new LineOfCode("itemOrder--;")
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("itemOrderMoveDown").annotate("@Override").addContent(
                        checkForItemOrderOperationOnUninitializedBean()
                ).addContent(EMPTY_LINE).addContent(
                        new IfBlock(new Condition(new FunctionCall("isLastItemOrder"))).addContent(
                                new ExceptionThrow("IllegalArgumentException").addArgument("\"Cannot move Item Order below max position: \" + itemOrder")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        getItemOrderFunctionCalls("itemOrderMoveDown", itemOrderField)
                ).addContent(EMPTY_LINE).addContent(
                        new LineOfCode("itemOrder++;")
                )
        ).addContent(EMPTY_LINE);

        if (itemOrderField.isUnique()) {
            javaClass.addContent(
                    new FunctionDeclaration("itemOrderMoveAfter").annotate("@Override")
                            .addArgument(new FunctionArgument(beanName, beanVarName))
                            .addContent(
                                    new IfBlock(new Condition(new Comparison("itemOrder", new FunctionCall("getItemOrder", beanVarName), Comparison.Comparator.GREATER_THAN))).addContent(
                                            new FunctionCall("itemOrderMove").byItself()
                                                    .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                                    .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQuery", parametersVar))
                                                    .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                    .addArgument("itemOrder")
                                    ).elseClause(new ElseBlock().addContent(
                                            new FunctionCall("itemOrderMove").byItself()
                                                    .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                    .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQuery", parametersVar))
                                                    .addArgument("itemOrder")
                                                    .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                    ))
                            )
            ).addContent(EMPTY_LINE).addContent(
                    new FunctionDeclaration("itemOrderMoveBefore").annotate("@Override")
                            .addArgument(new FunctionArgument(beanName, beanVarName))
                            .addContent(
                                    new IfBlock(new Condition(new Comparison("itemOrder", new FunctionCall("getItemOrder", beanVarName), Comparison.Comparator.GREATER_THAN))).addContent(
                                            new FunctionCall("itemOrderMove").byItself()
                                                    .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                    .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQuery", parametersVar))
                                                    .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                    .addArgument("itemOrder")
                                    ).elseClause(new ElseBlock().addContent(
                                            new FunctionCall("itemOrderMove").byItself()
                                                    .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                    .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQuery", parametersVar))
                                                    .addArgument("itemOrder")
                                                    .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                    ))
                            )
            ).addContent(EMPTY_LINE).addContent(
                    getBaseItemOrderMoveFunction().addContent(
                            new FunctionCall("updateItemOrdersInBetween", "DBQueries").addArguments("query", "transaction", "lowerBound", "upperBound").byItself()
                    ).addContent(
                            getItemOrderCompleteTransactionFunctionCall()
                    )
            ).addContent(EMPTY_LINE);
        } else {
            final String associatedFieldJavaName = uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField()));
            final String associatedFieldJavaFunction = "get" + camelize(itemOrderField.getItemOrderAssociatedField());
            javaClass.addContent(
                    new FunctionDeclaration("itemOrderMoveAfter").annotate("@Override").addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, new FunctionCall(associatedFieldJavaFunction, beanVarName)))).addContent(
                                    new IfBlock(new Condition(new Comparison("itemOrder", new FunctionCall("getItemOrder", beanVarName), Comparison.Comparator.GREATER_THAN))).addContent(
                                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                                            .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQueryWithNullSecondaryField", parametersVar))
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument("itemOrder")
                                            ).elseClause(new ElseBlock().addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                                            .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQuery", parametersVar))
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument("itemOrder")
                                                            .addArgument(associatedFieldJavaName)
                                            ))
                                    ).elseClause(new ElseBlock().addContent(
                                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQueryWithNullSecondaryField", parametersVar))
                                                            .addArgument("itemOrder")
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                            ).elseClause(new ElseBlock().addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQuery", parametersVar))
                                                            .addArgument("itemOrder")
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                                            .addArgument(associatedFieldJavaName)
                                            ))
                                    ))
                            ).elseClause(new ElseBlock().addContent(
                                    new FunctionCall("itemOrderMove").byItself()
                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.ADD))
                                            .addArgument(beanVarName)
                            ))
                    )
            ).addContent(EMPTY_LINE).addContent(
                    new FunctionDeclaration("itemOrderMoveBefore").annotate("@Override").addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, new FunctionCall(associatedFieldJavaFunction, beanVarName)))).addContent(
                                    new IfBlock(new Condition(new Comparison("itemOrder", new FunctionCall("getItemOrder", beanVarName), Comparison.Comparator.GREATER_THAN))).addContent(
                                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQueryWithNullSecondaryField", parametersVar))
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                            .addArgument("itemOrder")
                                            ).elseClause(new ElseBlock().addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument(new FunctionCall("getIncreaseItemOrderBetweenQuery", parametersVar))
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                            .addArgument("itemOrder")
                                                            .addArgument(associatedFieldJavaName)
                                            ))
                                    ).elseClause(new ElseBlock().addContent(
                                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                            .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQueryWithNullSecondaryField", parametersVar))
                                                            .addArgument("itemOrder")
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                            ).elseClause(new ElseBlock().addContent(
                                                    new FunctionCall("itemOrderMove").byItself()
                                                            .addArgument(new OperatorExpression(new FunctionCall("getItemOrder", beanVarName), "1", OperatorExpression.Operator.SUBTRACT))
                                                            .addArgument(new FunctionCall("getDecreaseItemOrderBetweenQuery", parametersVar))
                                                            .addArgument("itemOrder")
                                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                                            .addArgument(associatedFieldJavaName)
                                            ))
                                    ))
                            ).elseClause(new ElseBlock().addContent(
                                    new FunctionCall("itemOrderMove").byItself()
                                            .addArgument(new FunctionCall("getItemOrder", beanVarName))
                                            .addArgument(beanVarName)
                            ))
                    )
            ).addContent(EMPTY_LINE).addContent(
                    getBaseItemOrderMoveFunction().addArgument(new FunctionArgument("long...", "parameters")).addContent(
                            new FunctionCall("updateItemOrdersInBetween", "DBQueries").addArguments("query", "transaction", "lowerBound", "upperBound", "parameters").byItself()
                    ).addContent(
                            getItemOrderCompleteTransactionFunctionCall()
                    )
            ).addContent(EMPTY_LINE).addContent(
                    getItemOrderMoveDeclarationStart()
                            .addArgument(new FunctionArgument("long", "newItemOrder"))
                            .addArgument(new FunctionArgument(beanName, beanVarName))
                            .addContent(
                                    new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                            getItemOrderMoveCall()
                                                    .addArgument(new FunctionCall("getPushItemOrdersUpQueryWithNullSecondaryField", parametersVar))
                                                    .addArgument("newItemOrder - 1")
                                                    .addArgument(new FunctionCall("getPushItemOrdersDownQuery", parametersVar))
                                                    .addArgument(beanVarName)
                                    ).addElseIfClause(new ElseIfBlock(new Condition(new Comparison(new FunctionCall(associatedFieldJavaFunction, beanVarName), "0"))).addContent(
                                            getItemOrderMoveCall()
                                                    .addArgument(new FunctionCall("getPushItemOrdersUpQuery", parametersVar))
                                                    .addArgument("newItemOrder - 1")
                                                    .addArgument(new FunctionCall("getPushItemOrdersDownQueryWithNullSecondaryField", parametersVar))
                                                    .addArgument(beanVarName)
                                    )).elseClause(new ElseBlock().addContent(
                                            getItemOrderMoveCall()
                                                    .addArgument(new FunctionCall("getPushItemOrdersUpQuery", parametersVar))
                                                    .addArgument("newItemOrder - 1")
                                                    .addArgument(new FunctionCall("getPushItemOrdersDownQuery", parametersVar))
                                                    .addArgument(beanVarName)
                                    ))
                            )
            ).addContent(EMPTY_LINE).addContent(
                    getItemOrderMoveDeclarationStart()
                            .addArgument(new FunctionArgument("long", "newItemOrder"))
                            .addArgument(new FunctionArgument("String", "queryDest"))
                            .addArgument(new FunctionArgument("long", "destLowerBound"))
                            .addArgument(new FunctionArgument("String", "queryOrig"))
                            .addArgument(new FunctionArgument(beanName, beanVarName))
                            .addContent(
                                    new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal()
                            ).addContent(
                                    new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                            getDBQueriesUpdateItemOrdersAboveCall().addArguments("queryDest", "transaction", "destLowerBound")
                                    ).elseClause(new ElseBlock().addContent(
                                            getDBQueriesUpdateItemOrdersAboveCall().addArguments("queryDest", "transaction", "destLowerBound", associatedFieldJavaName)
                                    ))
                            ).addContent(
                                    new IfBlock(new Condition(new Comparison(new FunctionCall(associatedFieldJavaFunction, beanVarName), "0"))).addContent(
                                            getDBQueriesUpdateItemOrdersAboveCall().addArguments("queryOrig", "transaction", "itemOrder")
                                    ).elseClause(new ElseBlock().addContent(
                                            getDBQueriesUpdateItemOrdersAboveCall().addArguments("queryOrig", "transaction", "itemOrder", associatedFieldJavaName)
                                    ))
                            ).addContent(
                                    getItemOrderCompleteTransactionFunctionCall()
                            )
            ).addContent(EMPTY_LINE).addContent(
                    new FunctionDeclaration("itemOrderReassociateWith").addArgument(new FunctionArgument("long", associatedFieldJavaName)).addContent(
                            new IfBlock(new Condition(new Comparison("id", "0"))).addContent(
                                    ExceptionThrow.getThrowExpression("IllegalArgumentException", "Bean must be saved in DB before reassociation.")
                            )
                    ).addContent(
                            new IfBlock(new Condition(new Comparison("this." + associatedFieldJavaName, associatedFieldJavaName))).addContent(
                                    ExceptionThrow.getThrowExpression("IllegalArgumentException", "Association already exists.")
                            )
                    ).addContent(EMPTY_LINE).addContent(
                            new FunctionCall("itemOrderMove").addArgument(associatedFieldJavaName).byItself()
                    )
            ).addContent(EMPTY_LINE).addContent(
                    getItemOrderMoveDeclarationStart().addArgument(new FunctionArgument("long", associatedFieldJavaName)).addContent(
                            new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal()
                    ).addContent(
                            new VarDeclaration("long", "newItemOrder")
                    ).addContent(
                            new IfBlock(new Condition(new Comparison(associatedFieldJavaName, "0"))).addContent(
                                    new Assignment("newItemOrder", new OperatorExpression(
                                            new FunctionCall("getMaxItemOrder", "DBQueries")
                                                    .addArgument("transaction")
                                                    .addArgument(new FunctionCall("getItemOrderMaxQueryWithNullSecondaryField", parametersVar)),
                                            "1",
                                            OperatorExpression.Operator.ADD))
                            ).elseClause(new ElseBlock().addContent(
                                    new Assignment("newItemOrder", new OperatorExpression(
                                            new FunctionCall("getMaxItemOrder", "DBQueries")
                                                    .addArgument("transaction")
                                                    .addArgument(new FunctionCall("getItemOrderMaxQuery", parametersVar))
                                                    .addArgument(associatedFieldJavaName),
                                            "1",
                                            OperatorExpression.Operator.ADD))
                            ))
                    ).addContent(
                            new IfBlock(new Condition(new Comparison("this." + associatedFieldJavaName, "0"))).addContent(
                                    new FunctionCall("updateItemOrdersAbove", "DBQueries").byItself()
                                            .addArgument(new FunctionCall("getUpdateItemOrdersAboveQueryWithNullSecondaryField", parametersVar))
                                            .addArgument("transaction").addArgument("itemOrder")
                            ).elseClause(new ElseBlock().addContent(
                                    new FunctionCall("updateItemOrdersAbove", "DBQueries").byItself()
                                            .addArgument(new FunctionCall("getUpdateItemOrdersAboveQuery", parametersVar))
                                            .addArgument("transaction").addArgument("itemOrder")
                                            .addArgument("this." + associatedFieldJavaName)
                            ))
                    ).addContent(
                            new Assignment("this." + associatedFieldJavaName, associatedFieldJavaName)
                    ).addContent(
                            getItemOrderCompleteTransactionFunctionCall()
                    )
            ).addContent(EMPTY_LINE);
        }

        javaClass.addContent(
                new FunctionDeclaration("itemOrderMoveCompleteTransaction").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("long", "newItemOrder"))
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addContent(new Assignment("itemOrder", "newItemOrder"))
                        .addContent(new FunctionCall("updateRecord").addArgument("transaction").byItself())
                        .addContent(new FunctionCall("commit", "transaction").byItself())
        ).addContent(EMPTY_LINE);
    }

    private FunctionDeclaration getItemOrderMoveDeclarationStart() {
        return new FunctionDeclaration("itemOrderMove").visibility(Visibility.PROTECTED);
    }

    private FunctionDeclaration getBaseItemOrderMoveFunction() {
        return getItemOrderMoveDeclarationStart()
                .addArgument(new FunctionArgument("long", "newItemOrder"))
                .addArgument(new FunctionArgument("String", "query"))
                .addArgument(new FunctionArgument("long", "lowerBound"))
                .addArgument(new FunctionArgument("long", "upperBound"))
                .addContent(new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal());
    }

    private FunctionCall getItemOrderMoveCall() {
        return new FunctionCall("itemOrderMove").byItself().addArgument("newItemOrder");
    }

    private FunctionCall getDBQueriesUpdateItemOrdersAboveCall() {
        return new FunctionCall("updateItemOrdersAbove", "DBQueries").byItself();
    }

    private FunctionCall getItemOrderCompleteTransactionFunctionCall() {
        return new FunctionCall("itemOrderMoveCompleteTransaction").addArguments("newItemOrder", "transaction").byItself();
    }

    private FunctionCall getMaxItemOrderFunctionCall(final Column itemOrderField, final boolean withTransaction, final boolean nullSecondaryVariant) {
        final FunctionCall functionCall = new FunctionCall("getMaxItemOrder", "DBQueries");

        if (withTransaction)
            functionCall.addArgument("transaction");
        else
            functionCall.addArgument("db");

        if (nullSecondaryVariant)
            functionCall.addArgument(new FunctionCall("getItemOrderMaxQueryWithNullSecondaryField", parametersVar));
        else {
            functionCall.addArgument(new FunctionCall("getItemOrderMaxQuery", parametersVar));
            if (!itemOrderField.isUnique())
                functionCall.addArgument(uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField())));
        }

        return functionCall;
    }

    private JavaCodeBlock getItemOrderFunctionCalls(final String functionName, final Column itemOrderField) {
        final FunctionCall itemOrderFunctionCall = getItemOrderFunctionCall(functionName, "getIdFromItemOrderQuery");
        if (itemOrderField.isUnique())
            return itemOrderFunctionCall;

        final String itemOrderAssociatedFieldJavaName = uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField()));
        itemOrderFunctionCall.addArgument(itemOrderAssociatedFieldJavaName);
        final FunctionCall itemOrderFunctionCallNullCase = getItemOrderFunctionCall(functionName, "getIdFromItemOrderQueryWithNullSecondaryField");

        return new IfBlock(new Condition(new Comparison(itemOrderAssociatedFieldJavaName, "0", Comparison.Comparator.GREATER_THAN)))
                .addContent(itemOrderFunctionCall)
                .elseClause(new ElseBlock().addContent(itemOrderFunctionCallNullCase));
    }

    private FunctionCall getItemOrderFunctionCall(final String functionName, final String parameterFunctionName) {
        return new FunctionCall(functionName, "DBQueries")
                .addArgument("db")
                .addArgument(new FunctionCall(parameterFunctionName, parametersVar))
                .addArgument(quickQuote(tableName))
                .addArgument("id")
                .addArgument("itemOrder")
                .byItself();
    }

    private IfBlock checkForItemOrderOperationOnUninitializedBean() {
        return new IfBlock(new Condition("id == 0")).addContent(
                new ExceptionThrow("IllegalArgumentException").addArgument(quickQuote("Item Order operations not allowed on beans that have not been saved to the database"))
        );
    }

    private static FunctionCall getPreUpdateConversionCall(final boolean withTransaction) {
        final FunctionCall functionCall = new FunctionCall("preUpdateConversions").byItself();

        if (withTransaction)
            functionCall.addArgument("transaction");

        return functionCall;
    }

    private void addInitSetLabelFunctions() {
        if (columns.hasLabels())
            for (Column column: columns.getList()) {
                if (column.isLabelReference()) {
                    final String choppedField = chopId(column.getJavaName());
                    final String localVar = uncapitalize(choppedField);
                    final String getIdFunction = "getId" + choppedField;
                    final String initFunction = "init" + choppedField;

                    javaClass.addContent(
                            new FunctionDeclaration(initFunction)
                                    .visibility(Visibility.PRIVATE)
                                    .addContent(
                                            new IfBlock(new Condition(new Comparison(localVar, "null")))
                                                    .addContent(
                                                            new Assignment(
                                                                    localVar,
                                                                    new FunctionCall(
                                                                            "createInstance",
                                                                            "Labels"))
                                                    )
                                                    .addContent(
                                                            new IfBlock(new Condition(new Comparison(
                                                                    new FunctionCall(getIdFunction),
                                                                    "0",
                                                                    Comparison.Comparator.GREATER_THAN)))
                                                                    .addContent(
                                                                            new FunctionCall("setId", localVar)
                                                                                    .addArgument(new FunctionCall(getIdFunction))
                                                                                    .byItself()
                                                                    )
                                                                    .addContent(
                                                                            new FunctionCall("cacheLabelsFromDB", localVar)
                                                                                    .byItself()
                                                                    )
                                                    )
                                    )
                    ).addContent(EMPTY_LINE);

                    javaClass.addContent(
                            new FunctionDeclaration("set" + choppedField)
                                    .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                                    .addArgument(new FunctionArgument("String", "text"))
                                    .addContent(
                                            new FunctionCall(initFunction).byItself()
                                    )
                                    .addContent(
                                            new FunctionCall("updateLater", localVar)
                                                    .addArguments("dbBeanLanguage", "text")
                                                    .byItself()
                                    )
                    ).addContent(EMPTY_LINE);
                }
            }
    }

    private void addUpdateDB() {
        if (columns.hasLastUpdate())
            javaClass.addContent(
                    new FunctionDeclaration("isUpdateOK", "boolean").addContent(
                            new ReturnStatement("updateOK")
                    )
            ).addContent(EMPTY_LINE);

        final FunctionDeclaration updateDBFunction = new FunctionDeclaration("updateDB").annotate("@Override");
        if (columns.hasModifiedBy())
            updateDBFunction.addArgument(new FunctionArgument("String", "username"));

        updateDBFunction.addContent(
                getPreUpdateConversionCall(false)
        ).addContent(EMPTY_LINE);

        final FunctionCall createRecordCall = new FunctionCall("createRecord").byItself();
        if (columns.hasModifiedBy())
            createRecordCall.addArgument("username");
        updateDBFunction.addContent(
                new IfBlock(new Condition("id == 0")).addContent(createRecordCall).addContent(new ReturnStatement())
        ).addContent(EMPTY_LINE);

        final FunctionCall updateRecordCall = new FunctionCall("updateRecord").byItself();
        if (columns.hasModifiedBy())
            updateRecordCall.addArgument("username");
        updateDBFunction.addContent(
                new IfBlock(new Condition("id > 0")).addContent(updateRecordCall).addContent(new ReturnStatement())
        ).addContent(EMPTY_LINE);

        updateDBFunction.addContent(
                new LineOfCode("assert (false) : \"id < 0 ?!?\";")
        );

        javaClass.addContent(updateDBFunction).addContent(EMPTY_LINE);


        final FunctionDeclaration updateDBFunctionWithTransaction =
                new FunctionDeclaration("updateDB", "long")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"));
        if (columns.hasModifiedBy())
            updateDBFunctionWithTransaction.addArgument(new FunctionArgument("String", "username"));

        updateDBFunctionWithTransaction.addContent(
                new IfBlock(new Condition(parametersClass + ".USE_CACHE && " + parametersClass + ".PREVENT_CACHE_USE_WITH_TRANSACTIONS"))
                        .addContent(
                                new ExceptionThrow("UnsupportedOperationException")
                                        .addArgument(quickQuote("Cannot cache intermediate updates."))
                        )
        ).addContent(EMPTY_LINE);

        updateDBFunctionWithTransaction.addContent(
                getPreUpdateConversionCall(true)
        ).addContent(EMPTY_LINE);

        final FunctionCall createRecordCallWithTransaction = new FunctionCall("createRecord").addArgument("transaction");
        if (columns.hasModifiedBy())
            createRecordCallWithTransaction.addArgument("username");
        updateDBFunctionWithTransaction.addContent(
                new IfBlock(new Condition("id == 0")).addContent(new Assignment("id", createRecordCallWithTransaction)).addContent(new ReturnStatement("id"))
        ).addContent(EMPTY_LINE);

        final FunctionCall updateRecordCallWithTransaction = new FunctionCall("updateRecord").addArgument("transaction").byItself();
        if (columns.hasModifiedBy())
            updateRecordCallWithTransaction.addArgument("username");
        updateDBFunctionWithTransaction.addContent(
                new IfBlock(new Condition("id > 0")).addContent(updateRecordCallWithTransaction).addContent(new ReturnStatement("id"))
        ).addContent(EMPTY_LINE);

        updateDBFunctionWithTransaction.addContent(
                new LineOfCode("assert (false) : \"id < 0 ?!?\";")
        ).addContent(
                new ReturnStatement("-1")
        );

        javaClass.addContent(updateDBFunctionWithTransaction).addContent(EMPTY_LINE);


        javaClass.addContent(
                new FunctionDeclaration("preUpdateConversions")
                        .annotate("@Override")
                        .addContent(
                                new FunctionCall("preUpdateConversions").addArgument("null").byItself()
                        )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration protectedPreUpdateConversionsFunction =
                new FunctionDeclaration("preUpdateConversions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"));

        protectedPreUpdateConversionsFunction.addContent(
                ifNotDataOKWithTransaction()
                        .addContent(
                                new ExceptionThrow("IllegalArgumentException")
                                        .addArgument(new FunctionCall("toStrings", "ErrorMessage")
                                                .addArgument(new FunctionCall("getErrorMessages")))
                        )
        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final boolean isIdReference = column.getSqlName().startsWith("id_");
                if ((type.equals("int") || type.equals("long")) && !isIdReference)
                    protectedPreUpdateConversionsFunction.addContent(
                            new Assignment(field,
                                    new FunctionCall("Strings.get" + capitalize(type) + "Val").addArgument(field + "Str"))
                    );
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    protectedPreUpdateConversionsFunction.addContent(
                            new IfBlock(new Condition(new FunctionCall("isEmpty", "Strings").addArgument(field + "Str"), true)).addContent(
                                    new Assignment(field, new FunctionCall("convertStringTo" + type).addArgument(field + "Str"))
                            ).elseClause(new ElseBlock().addContent(
                                    new Assignment(field, "null")
                            ))
                    );
                if (type.equals("Money"))
                    protectedPreUpdateConversionsFunction.addContent(
                            new Assignment(field, new ObjectCreation("Money")
                                    .addArgument(field + "Str")
                                    .addArgument(new FunctionCall("getDefaultMoneyFormat", parametersVar)))
                    );
            }
        }

        javaClass.addContent(protectedPreUpdateConversionsFunction).addContent(EMPTY_LINE);
    }

    private IfBlock ifNotDataOKWithTransaction() {
        return new IfBlock(new Condition(new FunctionCall("isDataOK").addArgument("transaction"), true));
    }

    private void addDataOK() {
        javaClass.addContent(
                new FunctionDeclaration("isDataOK", "boolean").annotate("@Override").addContent(
                        new ReturnStatement(new FunctionCall("isDataOK").addArgument("null"))
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration transactionDataOKFunction =
                new FunctionDeclaration("isDataOK", "boolean")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addContent(
                                new FunctionCall("clearErrorMessages", internalsVar).byItself()
                        );

        for (Column column: columns.getList())
            if (column.isLabelReference())
                transactionDataOKFunction.addContent(
                        new FunctionCall("init" + chopId(column.getJavaName()))
                                .byItself()
                );

        transactionDataOKFunction.addContent(
                new VarDeclaration("boolean", "ok", "true")
        ).addContent(EMPTY_LINE);

        int okAssignCount = 0;
        for (Column column: columns.getList())
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                transactionDataOKFunction.addContent(
                        new Assignment(
                                "ok",
                                dataOkOKUpdateExpression(
                                        column.getJavaName(),
                                        column.getSqlName().startsWith("id_"))));
                ++okAssignCount;
            }
        if (okAssignCount > 0)
            transactionDataOKFunction.addContent(EMPTY_LINE);

        transactionDataOKFunction.addContent(new ReturnStatement("ok"));

        javaClass.addContent(transactionDataOKFunction).addContent(EMPTY_LINE);

        // checkDataForField functions
        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {

                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);

                final FunctionDeclaration checkFieldFunction = getCheckFieldFunction(fieldCap);

                if (column.getSqlName().startsWith("id_")) {
                    javaClass.addContent(
                            getCheckFieldFunction(fieldCap).addContent(
                                    new ReturnStatement(new FunctionCall("checkDataFor" + fieldCap).addArgument("null"))
                            )
                    ).addContent(EMPTY_LINE);

                    checkFieldFunction.addArgument(new FunctionArgument("DBTransaction", "transaction"));
                }

                if (column.isLabelReference()) {
                    final String localVar = uncapitalize(chopId(field));
                    checkFieldFunction.addContent(
                            new ForLoop("DbBeanLanguage dbBeanLanguage: Labels.getAllActiveLanguages()")
                                    .addContent(
                                            new VarDeclaration(
                                                    "String",
                                                    "iso",
                                                    new FunctionCall("getCapIso", "dbBeanLanguage"))
                                                    .markAsFinal()
                                    )
                                    .addContent(
                                            new IfBlock(new Condition(
                                                    new FunctionCall("isEmpty", "Strings")
                                                            .addArgument(new FunctionCall("get", localVar)
                                                                    .addArgument("dbBeanLanguage"))))
                                                    .addContent(
                                                            new IfBlock(new Condition(
                                                                    new FunctionCall("is" + fieldCap + "Required")
                                                                            .addArgument("dbBeanLanguage")))
                                                                    .addContent(
                                                                            new FunctionCall("addErrorMessage", internalsVar)
                                                                                    .byItself()
                                                                                    .addArgument("id")
                                                                                    .addArgument(quickQuote(field) + " + iso")
                                                                                    .addArgument(getLabelLabelComposition(fieldCap))
                                                                                    .addArgument(new FunctionCall("get" + fieldCap + "EmptyErrorMessage"))
                                                                    )
                                                                    .addContent(new ReturnStatement("false"))
                                                    )
                                    )
                    ).addContent(EMPTY_LINE).addContent(new ReturnStatement("true"));
                } else {
                    final IfBlock checkRequired =
                            new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "Empty"))).addContent(
                                    new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "Required"))).addContent(
                                            new FunctionCall("addErrorMessage", internalsVar).byItself()
                                                    .addArgument("id")
                                                    .addArgument(quickQuote(field))
                                                    .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                                    .addArgument(new FunctionCall("get" + fieldCap + "EmptyErrorMessage"))
                                    ).addContent(new ReturnStatement("false"))
                            );

                    final FunctionCall isOKFunctionCall = new FunctionCall("is" + fieldCap + "OK");
                    if (column.getSqlName().startsWith("id_"))
                        isOKFunctionCall.addArgument("transaction");

                    final ElseIfBlock checkOK =
                            new ElseIfBlock(new Condition(isOKFunctionCall, true)).addContent(
                                    new FunctionCall("addErrorMessage", internalsVar).byItself()
                                            .addArgument("id")
                                            .addArgument(quickQuote(field))
                                            .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                            .addArgument(new FunctionCall("get" + fieldCap + "BadFormatErrorMessage"))
                            ).addContent(new ReturnStatement("false"));

                    final ElseIfBlock checkUnique =
                            new ElseIfBlock(
                                    new Condition(new FunctionCall("is" + fieldCap + "ToBeUnique"))
                                            .andCondition(
                                                    new Condition(new FunctionCall("is" + fieldCap + "Unique"), true)))
                                    .addContent(
                                            new FunctionCall("addErrorMessage", internalsVar).byItself()
                                                    .addArgument("id")
                                                    .addArgument(quickQuote(field))
                                                    .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                                    .addArgument(new FunctionCall("get" + fieldCap + "NotUniqueErrorMessage"))
                                    ).addContent(new ReturnStatement("false"));

                    checkFieldFunction
                            .addContent(checkRequired.addElseIfClause(checkOK).addElseIfClause(checkUnique))
                            .addContent(EMPTY_LINE)
                            .addContent(new ReturnStatement("true"));
                }

                javaClass.addContent(checkFieldFunction).addContent(EMPTY_LINE);
            }
        }

        // isFieldEmpty() functions
        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);
                final boolean isIdReference = column.getSqlName().startsWith("id_");

                final ReturnStatement returnStatement;
                if ((type.equals("int") || type.equals("long")) && isIdReference)
                    returnStatement = new ReturnStatement(new Comparison(field, "0", Comparison.Comparator.EQUAL));
                else {
                    final String arg;
                    if (type.equals("String"))
                        arg = field;
                    else
                        arg = field + "Str";
                    returnStatement = new ReturnStatement(new FunctionCall("isEmpty", "Strings").addArgument(arg));
                }

                javaClass.addContent(
                        new FunctionDeclaration("is" + fieldCap + "Empty", "boolean").addContent(returnStatement)
                ).addContent(EMPTY_LINE);
            }
        }

        // getFieldEmptyErrorMessage() functions
        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);

                javaClass.addContent(
                        new FunctionDeclaration("get" + fieldCap + "EmptyErrorMessage", "String").addContent(
                                new ReturnStatement(new FunctionCall("getRequiredErrorMessage", internalsVar).addArgument(quickQuote(field)))
                        )
                ).addContent(EMPTY_LINE);
            }
        }

        // isFieldOK() functions
        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);
                final boolean isIdReference = column.getSqlName().startsWith("id_");

                final FunctionDeclaration isOKFunction = getIsFieldOKFunction(fieldCap);

                if (isIdReference) {
                    javaClass.addContent(
                            getIsFieldOKFunction(fieldCap).addContent(
                                    new ReturnStatement(new FunctionCall("is" + fieldCap + "OK").addArgument("null"))
                            )
                    ).addContent(EMPTY_LINE);

                    isOKFunction.visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("DBTransaction", "transaction"));
                }

                if ((type.equals("int") || type.equals("long"))) {
                    if (isIdReference) {
                        if (column.isLabelReference())
                            addOKFunctionBodyForBeanReference(isOKFunction, "Labels", field);
                        else if (column.isFileReference())
                            addOKFunctionBodyForBeanReference(isOKFunction, "LocalFiles", field);
                        else
                            addOKFunctionBodyForBeanReference(isOKFunction, column.getAssociatedBeanClass(), field);
                    } else {
                        importsManager.addImport("org.beanmaker.util.FormatCheckHelper");
                        isOKFunction.addContent(
                                new ReturnStatement(
                                        new FunctionCall("is" + capitalize(type) + "Number", "FormatCheckHelper")
                                                .addArgument(field + "Str")));
                    }
                }

                else if (JAVA_TEMPORAL_TYPES.contains(type)) {
                    isOKFunction.addContent(new ReturnStatement(new FunctionCall("validate" + type + "Format").addArgument(field + "Str")));
                }

                else if (type.equals("String")) {
                    if (field.equalsIgnoreCase("email") || field.equalsIgnoreCase("e-mail")) {
                        importsManager.addImport("org.beanmaker.util.FormatCheckHelper");
                        isOKFunction.addContent(new ReturnStatement(new FunctionCall("isEmailValid", "FormatCheckHelper").addArgument(field)));
                    } else
                        isOKFunction.addContent(new ReturnStatement("true"));
                }

                else if (type.equals("Money")) {
                    isOKFunction.addContent(
                            new ReturnStatement(new FunctionCall("isValOK", parametersVar + ".getDefaultMoneyFormat()")
                                    .addArgument(field + "Str"))
                    );
                }

                else
                    throw new IllegalStateException("Apparently unsupported type " + type + " encountered.");

                javaClass.addContent(isOKFunction).addContent(EMPTY_LINE);
            }
        }

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);
                javaClass.addContent(
                        new FunctionDeclaration("get" + fieldCap + "BadFormatErrorMessage", "String").addContent(
                                new ReturnStatement(new FunctionCall("getBadFormatErrorMessage", internalsVar).addArgument(quickQuote(field)))
                        )
                ).addContent(EMPTY_LINE);
            }
        }

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {

                final FunctionCall setComparisonArgument;
                if (column.getJavaType().equals("Money"))
                    setComparisonArgument = new FunctionCall("setLong", "stat")
                            .addArgument("1")
                            .addArgument(new FunctionCall("getVal", column.getJavaName()))
                            .byItself();
                else
                    setComparisonArgument = new FunctionCall("set" + capitalize(column.getJavaType()), "stat")
                            .addArgument("1")
                            .addArgument(column.getJavaName())
                            .byItself();

                final FunctionCall dbAccessFunctionCall = new FunctionCall("processQuery", "!dbAccess");
                javaClass.addContent(
                        new FunctionDeclaration("is" + capitalize(column.getJavaName() + "Unique"), "boolean")
                                .addContent(
                                        new ReturnStatement(
                                                dbAccessFunctionCall
                                                        .addArgument(Strings.quickQuote(getNotUniqueQuery(column)))
                                                        .addArgument(new AnonymousClassCreation("BooleanCheckQuery")
                                                                .setContext(dbAccessFunctionCall, 1)
                                                                .addContent(
                                                                        new FunctionDeclaration("setupPreparedStatement")
                                                                                .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                                                                                .annotate("@Override").addException("SQLException")
                                                                                .addContent(setComparisonArgument)
                                                                                .addContent(
                                                                                        new FunctionCall("setLong", "stat")
                                                                                                .addArguments("2", "id").byItself()
                                                                                )
                                                                ))
                                        )
                                )
                ).addContent(EMPTY_LINE);
            }
        }

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);
                javaClass.addContent(
                        new FunctionDeclaration("get" + fieldCap + "NotUniqueErrorMessage", "String").addContent(
                                new ReturnStatement(new FunctionCall("getNotUniqueErrorMessage", internalsVar).addArgument(quickQuote(field)))
                        )
                ).addContent(EMPTY_LINE);
            }
        }

        javaClass.addContent(
                new FunctionDeclaration("getErrorMessages", "List<ErrorMessage>").annotate("@Override").addContent(
                        new ReturnStatement(new FunctionCall("getErrorMessages", internalsVar))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addOKFunctionBodyForBeanReference(
            final FunctionDeclaration isOKFunction,
            final String beanClass,
            final String field)
    {
        isOKFunction.addContent(
                new IfBlock(new Condition("transaction == null")).addContent(
                        new ReturnStatement(
                                new FunctionCall("isIdOK", beanClass)
                                        .addArgument(field))
                )
        ).addContent(EMPTY_LINE).addContent(
                new ReturnStatement(
                        new FunctionCall("isIdOK", beanClass)
                                .addArguments(field, "transaction"))
        );
    }

    private FunctionDeclaration getCheckFieldFunction(final String fieldCap) {
        return new FunctionDeclaration("checkDataFor" + fieldCap, "boolean")
                .visibility(Visibility.PROTECTED);
    }

    private String dataOkOKUpdateExpression(final String varName, final boolean referenceToOtherBean) {
        if (referenceToOtherBean)
            return "checkDataFor" + capitalize(varName) + "(transaction) && ok";

        return "checkDataFor" + capitalize(varName) + "() && ok";
    }

    private FunctionDeclaration getIsFieldOKFunction(String fieldCap) {
        return new FunctionDeclaration("is" + fieldCap + "OK", "boolean");
    }

    private FunctionCall getOtherBeanCheckIdFunctionCall(final String associatedBeanClass, final String field) {
        return new FunctionCall("isIdOK", associatedBeanClass).addArgument(field);
    }

    private String getLabelLabelComposition(final String fieldCap) {
        return "get" + fieldCap + "Label() + \" \" + iso";
    }

    private String getNotUniqueQuery(final Column column) {
        return "SELECT id FROM " + tableName + " WHERE " + backquote(column.getSqlName()) + "=? AND id <> ?";
    }

    private void addReset() {
        final FunctionDeclaration resetFunction = new FunctionDeclaration("reset").annotate("@Override");

        boolean hasLabels = false;
        for (Column column: columns.getList())
            if (column.isLabelReference()) {
                resetFunction.addContent(
                        new FunctionCall("init" + chopId(column.getJavaName()))
                                .byItself()
                );
                hasLabels = true;
            }

        if (hasLabels)
            resetFunction.addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final boolean isIdReference = column.getSqlName().startsWith("id_");

                if (type.equals("boolean"))
                    resetFunction.addContent(new Assignment(field, "false"));
                else if (type.equals("int") || type.equals("long"))
                    resetFunction.addContent(new Assignment(field, "0"));
                else if (type.equals("String"))
                    resetFunction.addContent(new Assignment(field, EMPTY_STRING));
                else if (type.equals("Money"))
                    resetFunction.addContent(new Assignment(field, new ObjectCreation("Money")
                            .addArgument("0")
                            .addArgument(new FunctionCall("getDefaultMoneyFormat", parametersVar))));
                else
                    resetFunction.addContent(new Assignment(field, "null"));

                if (JAVA_TEMPORAL_TYPES.contains(type)
                        || type.equals("Money")
                        || ((type.equals("int")
                        || type.equals("int"))
                        && !isIdReference))
                    resetFunction.addContent(new Assignment(field + "Str", EMPTY_STRING));
            }
        }

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                resetFunction.addContent(new FunctionCall("clear", relationship.getJavaName()).byItself());


        resetFunction.addContent(new FunctionCall("clearErrorMessages", internalsVar).byItself());

        if (hasLabels) {
            resetFunction.addContent(EMPTY_LINE);

            for (Column column: columns.getList())
                if (column.isLabelReference())
                    resetFunction.addContent(
                            new FunctionCall("clearCache", uncapitalize(chopId(column.getJavaName())))
                                    .byItself()
                    );
        }

        javaClass.addContent(resetFunction).addContent(EMPTY_LINE);


        final FunctionDeclaration fullResetFunction =
                new FunctionDeclaration("fullReset").annotate("@Override").addContent(
                        new FunctionCall("reset").byItself()
                ).addContent(
                        new Assignment("id", "0")
                );

        for (Column column: columns.getList()) {
            if (column.isLastUpdate())
                fullResetFunction.addContent(new Assignment("lastUpdate", "0"));
            if (column.isModifiedBy())
                fullResetFunction.addContent(new Assignment("modifiedBy", "null"));
            if (column.isItemOrder())
                fullResetFunction.addContent(new Assignment("itemOrder", "0"));
        }

        if (hasLabels) {
            fullResetFunction.addContent(EMPTY_LINE);

            for (Column column: columns.getList())
                if (column.isLabelReference())
                    fullResetFunction.addContent(
                            new FunctionCall("fullReset", uncapitalize(chopId(column.getJavaName())))
                                    .byItself()
                    );
        }

        javaClass.addContent(fullResetFunction).addContent(EMPTY_LINE);
    }

    private void addDelete() {
        final FunctionDeclaration deleteFunction =
                new FunctionDeclaration("delete")
                        .annotate("@Override");

        final FunctionCall accessDB =
                new FunctionCall("addUpdate", "transaction")
                        .byItself();

        deleteFunction.addContent(
                new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction"))
                        .markAsFinal()
        ).addContent(
                new FunctionCall("delete").addArguments("transaction", "false").byItself()
        );

        deleteFunction.addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(
                EMPTY_LINE
        ).addContent(
                new FunctionCall("postDeleteActions").byItself()
        ).addContent(
                EMPTY_LINE
        ).addContent(
                new IfBlock(new Condition(parametersClass + ".USE_CACHE")).addContent(
                        new FunctionCall("delete", parametersClass + ".CACHE_SET")
                                .addArgument("id")
                                .byItself()
                )
        ).addContent(
                EMPTY_LINE
        ).addContent(
                new FunctionCall("fullReset").byItself()
        );

        javaClass.addContent(deleteFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("delete")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addContent(
                                new FunctionCall("delete")
                                        .byItself()
                                        .addArguments("transaction", "true")
                        )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration deleteFunctionWithTransaction =
                new FunctionDeclaration("delete")
                        .visibility(Visibility.PRIVATE)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addArgument(new FunctionArgument("boolean", "checkCacheStatus"))
                        .addContent(
                                new IfBlock(new Condition(getCacheCheckDeleteCondition()))
                                        .addContent(
                                                new ExceptionThrow("UnsupportedOperationException")
                                                        .addArgument(quickQuote("Cannot delete bean in transaction if caching is active."))
                                        )
                        ).addContent(EMPTY_LINE).addContent(
                                new FunctionCall("preDeleteExtraDbActions").byItself().addArgument("transaction")
                        );

        if (columns.hasItemOrder())
            deleteFunctionWithTransaction.addContent(
                    new VarDeclaration("long", "curItemOrder").markAsFinal()
            ).addContent(
                    new IfBlock(new Condition(new FunctionCall("isLastItemOrder"))).addContent(
                            new Assignment("curItemOrder", "0")
                    ).elseClause(new ElseBlock().addContent(
                            new Assignment("curItemOrder", "itemOrder")
                    ))
            );

        deleteFunctionWithTransaction.addContent(
                accessDB.addArgument(quickQuote(getDeleteSQLQuery()))
                        .addArgument(
                                new AnonymousClassCreation("DBQuerySetup")
                                        .setContext(accessDB)
                                        .addContent(
                                                new FunctionDeclaration("setupPreparedStatement")
                                                        .annotate("@Override")
                                                        .addException("SQLException")
                                                        .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                                                        .addContent(
                                                                new FunctionCall("setLong", "stat")
                                                                        .addArguments("1", "id")
                                                                        .byItself()
                                                        )
                                        ))
        );

        if (columns.hasItemOrder()) {
            final IfBlock checkItemOrderNotMax =
                    new IfBlock(
                            new Condition(
                                    new Comparison("curItemOrder", "0", Comparison.Comparator.GREATER_THAN)));

            final Column itemOrderField = columns.getItemOrderField();
            if (itemOrderField.isUnique())
                checkItemOrderNotMax.addContent(
                        getUpdateItemOrderAboveFunctionCall("getUpdateItemOrdersAboveQuery", null)
                );
            else {
                final String itemOrderAssociatedField =
                        uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField()));
                checkItemOrderNotMax.addContent(
                        new IfBlock(new Condition(new Comparison(itemOrderAssociatedField, "0"))).addContent(
                                getUpdateItemOrderAboveFunctionCall(
                                        "getUpdateItemOrdersAboveQueryWithNullSecondaryField",
                                        null)
                        ).elseClause(new ElseBlock().addContent(
                                getUpdateItemOrderAboveFunctionCall(
                                        "getUpdateItemOrdersAboveQuery",
                                        itemOrderAssociatedField)
                        )));
            }

            deleteFunctionWithTransaction.addContent(checkItemOrderNotMax);
        }

        deleteFunctionWithTransaction.addContent(
                new FunctionCall("deleteExtraDbActions").byItself().addArgument("transaction")
        );

        javaClass.addContent(deleteFunctionWithTransaction).addContent(EMPTY_LINE);


        javaClass.addContent(
                new FunctionDeclaration("preDeleteExtraDbActions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("deleteExtraDbActions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postDeleteActions")
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
    }

    private String getCacheCheckDeleteCondition() {
        return "checkCacheStatus && " + parametersClass + ".USE_CACHE && " + parametersClass + ".PREVENT_CACHE_USE_WITH_TRANSACTIONS";
    }

    private FunctionCall getUpdateItemOrderAboveFunctionCall(
            final String queryRetrievalFunction,
            final String itemOrderAssociatedField)
    {
        final FunctionCall functionCall = new FunctionCall("updateItemOrdersAbove", "DBQueries")
                .byItself()
                .addArgument(new FunctionCall(queryRetrievalFunction, parametersVar))
                .addArgument("transaction")
                .addArgument("curItemOrder");

        if (itemOrderAssociatedField != null)
            functionCall.addArgument(itemOrderAssociatedField);

        return functionCall;
    }

    private FunctionCall getStatSetFunction(final String type, final String field, final int index) {
        return new FunctionCall("set" + capitalize(type), "stat")
                .byItself()
                .addArguments(Integer.toString(index), field);
    }

    private JavaCodeBlock getFieldCreationOrUpdate(final Column column, final int index) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (column.isRequired())
            return getStatSetFunction(type, field, index);

        if (column.hasAssociatedBean())
            return new IfBlock(new Condition(new Comparison(field, "0"))).addContent(
                    new FunctionCall("setNull", "stat")
                            .byItself()
                            .addArguments(Integer.toString(index), "java.sql.Types.INTEGER")
            ).elseClause(new ElseBlock().addContent(
                    getStatSetFunction(type, field, index)
            ));

        if (JAVA_TEMPORAL_TYPES.contains(type))
            return new IfBlock(new Condition(new Comparison(field, "null"))).addContent(
                    new FunctionCall("setNull", "stat")
                            .byItself()
                            .addArguments(Integer.toString(index), "java.sql.Types." + type.toUpperCase())
            ).elseClause(new ElseBlock().addContent(
                    getStatSetFunction(type, field, index)
            ));

        return getStatSetFunction(type, field, index);
    }

    private void addCreate() {
        int index = 0;

        final JavaClass recordCreationSetupClass =
                new JavaClass("RecordCreationSetup")
                        .implementsInterface("DBQuerySetup")
                        .visibility(Visibility.PRIVATE);
        final FunctionDeclaration setupStatFunction =
                new FunctionDeclaration("setupPreparedStatement")
                        .annotate("@Override")
                        .addException("SQLException")
                        .addArgument(new FunctionArgument("PreparedStatement", "stat"));

        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!column.isId()) {
                if (type.equals("Money")) {
                    final String suggestedType = Column.getSuggestedType(column.getSqlTypeName(), column.getPrecision());
                    if (suggestedType.equals("int"))
                        setupStatFunction.addContent(
                                new FunctionCall("setInt", "stat")
                                        .byItself()
                                        .addArgument(Integer.toString(++index))
                                        .addArgument(new FunctionCall("getIntVal", field))
                        );
                    else if (suggestedType.equals("long"))
                        setupStatFunction.addContent(
                                new FunctionCall("setLong", "stat")
                                        .byItself()
                                        .addArgument(Integer.toString(++index))
                                        .addArgument(new FunctionCall("getVal", field))
                        );
                    else
                        throw new IllegalStateException("Money cannot be used with non INTEGER SQL field: " + column.getSqlName());
                } else {
                    setupStatFunction.addContent(
                            getFieldCreationOrUpdate(column, ++index)
                    );
                }
            }
        }

        javaClass.addContent(recordCreationSetupClass.addContent(setupStatFunction)).addContent(EMPTY_LINE);

        javaClass.addContent(
                new JavaClass("RecordUpdateSetup")
                        .extendsClass("RecordCreationSetup")
                        .visibility(Visibility.PRIVATE)
                        .addContent(
                                new FunctionDeclaration("setupPreparedStatement")
                                        .annotate("@Override")
                                        .addException("SQLException")
                                        .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                                        .addContent(
                                                new FunctionCall("setupPreparedStatement", "super")
                                                        .byItself()
                                                        .addArgument("stat")
                                        )
                                        .addContent(
                                                new FunctionCall("setLong", "stat")
                                                        .byItself()
                                                        .addArguments(Integer.toString(++index), "id")
                                        )
                        )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration createRecordFunction =
                new FunctionDeclaration("createRecord")
                        .visibility(Visibility.PRIVATE);

        createRecordFunction.addContent(
                new VarDeclaration(
                        "DBTransaction",
                        "transaction",
                        new FunctionCall("createDBTransaction")
                ).markAsFinal()
        );

        createRecordFunction.addContent(
                new Assignment("id", new FunctionCall("createRecord").addArgument("transaction"))
        );

        createRecordFunction.addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(
                new FunctionCall("postCreateActions").byItself()
        ).addContent(
                new FunctionCall("updateCaching").byItself()
        );

        javaClass.addContent(createRecordFunction).addContent(EMPTY_LINE);


        final FunctionDeclaration createRecordFunctionWithTransaction =
                new FunctionDeclaration("createRecord", "long")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .visibility(Visibility.PRIVATE);

        createRecordFunctionWithTransaction.addContent(
                new FunctionCall("preCreateExtraDbActions")
                        .byItself()
                        .addArgument("transaction")
        );

        if (columns.hasItemOrder()) {
            final Column itemOrderField = columns.getItemOrderField();
            final IfBlock uninitializedItemOrderCase =
                    new IfBlock(new Condition(new Comparison("itemOrder", "0")));
            if (itemOrderField.isUnique())
                uninitializedItemOrderCase.addContent(
                        new Assignment("itemOrder",
                                new OperatorExpression(
                                        getMaxItemOrderFunctionCall(
                                                columns.getItemOrderField(),
                                                true,
                                                false),
                                        "1",
                                        OperatorExpression.Operator.ADD))
                );
            else {
                uninitializedItemOrderCase.addContent(
                        new IfBlock(
                                new Condition(
                                        new Comparison(
                                                uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField())),
                                                "0"))
                        ).addContent(
                                new Assignment(
                                        "itemOrder",
                                        new OperatorExpression(
                                                getMaxItemOrderFunctionCall(
                                                        columns.getItemOrderField(),
                                                        true,
                                                        true),
                                                "1",
                                                OperatorExpression.Operator.ADD))
                        ).elseClause(new ElseBlock().addContent(
                                new Assignment(
                                        "itemOrder",
                                        new OperatorExpression(
                                                getMaxItemOrderFunctionCall(
                                                        columns.getItemOrderField(),
                                                        true,
                                                        false),
                                                "1",
                                                OperatorExpression.Operator.ADD))
                        ))
                );
            }

            createRecordFunctionWithTransaction.addContent(uninitializedItemOrderCase).addContent(EMPTY_LINE);
        }

        if (columns.hasLabels())
            addSetLabelIdFunctionCalls(createRecordFunctionWithTransaction);

        createRecordFunctionWithTransaction.addContent(
                new VarDeclaration(
                        "long",
                        "id",
                        new FunctionCall("addRecordCreation", "transaction")
                                .addArgument(quickQuote(getInsertSQLQuery()))
                                .addArgument(new ObjectCreation("RecordCreationSetup"))
                ).markAsFinal()
        );

        if (columns.hasLabels()) {
            createRecordFunctionWithTransaction.addContent(
                    new FunctionCall("updateLabels")
                            .byItself()
                            .addArgument("transaction")
            );
        }

        addOneToManyRelationshipDBUpdateFunctionCalls(createRecordFunctionWithTransaction);

        createRecordFunctionWithTransaction.addContent(
                new FunctionCall("createExtraDbActions").byItself().addArguments("transaction", "id")
        );

        createRecordFunctionWithTransaction.addContent(
                new ReturnStatement("id")
        );

        javaClass.addContent(createRecordFunctionWithTransaction).addContent(EMPTY_LINE);


        javaClass.addContent(
                new FunctionDeclaration("preCreateExtraDbActions")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("createExtraDbActions")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addArgument(new FunctionArgument("long", "id"))
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postCreateActions")
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
    }

    private void addOneToManyRelationshipDBUpdateFunctionCalls(final FunctionDeclaration function) {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            if (!relationship.isListOnly())
                function.addContent(
                        new FunctionCall("update" + capitalize(relationship.getJavaName()) + "InDB")
                                .byItself()
                                .addArguments("transaction", "id")
                );
        }
    }

    private void addInitLabelFunctionCalls(final FunctionDeclaration functionDeclaration) {
        for (Column column: columns.getList())
            if (column.isLabelReference())
                functionDeclaration.addContent(
                        new FunctionCall("init" + chopId(column.getJavaName()))
                                .byItself()
                );
    }

    private void addSetLabelIdFunctionCalls(final FunctionDeclaration createUpdateRecordFunction) {
        for (Column column: columns.getList())
            if (column.isLabelReference()) {
                final String field = column.getJavaName();
                createUpdateRecordFunction.addContent(
                        new FunctionCall("set" + capitalize(field))
                                .byItself()
                                .addArgument(new FunctionCall("updateDB", uncapitalize(chopId(field)))
                                        .addArgument("transaction")
                                )
                );
            }
    }

    private void addUpdate() {

        final FunctionDeclaration updateRecordFunction = new FunctionDeclaration("updateRecord")
                .visibility(Visibility.PRIVATE)
                .addContent(
                        new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction"))
                                .markAsFinal()
                );

        updateRecordFunction.addContent(
                new FunctionCall("updateRecord").byItself().addArgument("transaction")
        );

        updateRecordFunction.addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(
                new FunctionCall("postUpdateActions").byItself()
        ).addContent(
                new FunctionCall("updateCaching").byItself()
        );

        javaClass.addContent(updateRecordFunction).addContent(EMPTY_LINE);


        final FunctionDeclaration updateRecordFunctionWithTransaction =
                new FunctionDeclaration("updateRecord")
                        .visibility(Visibility.PRIVATE)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"));

        updateRecordFunctionWithTransaction.addContent(
                new FunctionCall("preUpdateExtraDbActions")
                        .byItself()
                        .addArgument("transaction")
        );

        if (columns.hasLabels()) {
            addInitLabelFunctionCalls(updateRecordFunctionWithTransaction);
            addSetLabelIdFunctionCalls(updateRecordFunctionWithTransaction);
        }

        updateRecordFunctionWithTransaction.addContent(
                new FunctionCall("addUpdate", "transaction")
                        .byItself()
                        .addArgument(quickQuote(getUpdateSQLQuery()))
                        .addArgument(new ObjectCreation("RecordUpdateSetup"))
        );

        addOneToManyRelationshipDBUpdateFunctionCalls(updateRecordFunctionWithTransaction);

        if (columns.hasLabels())
            updateRecordFunctionWithTransaction.addContent(
                    new FunctionCall("updateLabels")
                            .byItself()
                            .addArgument("transaction")
            );

        updateRecordFunctionWithTransaction.addContent(
                new FunctionCall("updateExtraDbActions").byItself().addArgument("transaction")
        );

        javaClass.addContent(updateRecordFunctionWithTransaction).addContent(EMPTY_LINE);


        javaClass.addContent(
                new FunctionDeclaration("preUpdateExtraDbActions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("updateExtraDbActions")
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postUpdateActions")
                        .visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
    }

    private void addUpdateLabels() {
        if (columns.hasLabels()) {
            final FunctionDeclaration updateLabelsFunction =
                    new FunctionDeclaration("updateLabels")
                            .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                            .visibility(Visibility.PRIVATE);

            for (Column column: columns.getList())
                if (column.isLabelReference())
                    updateLabelsFunction.addContent(
                            new FunctionCall("commitTextsToDatabase", uncapitalize(chopId(column.getJavaName())))
                                    .byItself()
                                    .addArgument("transaction")
                    );

            javaClass.addContent(updateLabelsFunction).addContent(EMPTY_LINE);
        }
    }

    private void addUpdateCaching() {
        javaClass.addContent(
                new FunctionDeclaration("updateCaching").visibility(Visibility.PRIVATE).addContent(
                        new IfBlock(new Condition(parametersClass + ".USE_CACHE")).addContent(
                                new FunctionCall("submit", parametersClass + ".CACHE_SET")
                                        .addArgument("(" + beanName + ") this")
                                        .byItself()
                        )
                )
        ).addContent(EMPTY_LINE);
    }

    private void addOneToManyRelationshipInDB() {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            if (!relationship.isListOnly()) {
                final FunctionDeclaration updateRelationshipFunction =
                        new FunctionDeclaration("update" + capitalize(relationship.getJavaName()) + "InDB")
                                .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                                .addArgument(new FunctionArgument("long", "id"))
                                .visibility(Visibility.PRIVATE);

                final FunctionCall dbAccessFunction =
                        new FunctionCall("addUpdate", "transaction")
                                .byItself();
                updateRelationshipFunction.addContent(
                        dbAccessFunction
                                .addArgument(
                                        quickQuote(getDeleteOneToManyRelationshipQuery(
                                                relationship.getTable(),
                                                relationship.getIdSqlName())))
                                .addArgument(
                                        new AnonymousClassCreation("DBQuerySetup")
                                                .setContext(dbAccessFunction)
                                                .addContent(
                                                        new FunctionDeclaration("setupPreparedStatement")
                                                                .annotate("@Override")
                                                                .addException("SQLException")
                                                                .addArgument(
                                                                        new FunctionArgument(
                                                                                "PreparedStatement",
                                                                                "stat"))
                                                                .addContent(
                                                                        new FunctionCall("setLong", "stat")
                                                                                .byItself()
                                                                                .addArguments("1", "id")
                                                                )
                                                ))
                ).addContent(EMPTY_LINE);

                final String var = uncapitalize(relationship.getBeanClass());
                updateRelationshipFunction.addContent(
                        new ForLoop(relationship.getBeanClass() + " " + var + ": " + relationship.getJavaName())
                                .addContent(
                                        new FunctionCall("resetId", var).byItself()
                                )
                                .addContent(
                                        new FunctionCall("setId" + beanName, var).addArgument("id").byItself()
                                )
                                .addContent(
                                        new FunctionCall("updateDB", var).addArgument("transaction").byItself()
                                )
                );

                javaClass.addContent(updateRelationshipFunction).addContent(EMPTY_LINE);
            }
        }
    }

    private void addTemporalFunctions() {
        if (types.contains("Date")) {
            javaClass.addContent(
                    new FunctionDeclaration("formatDate", "String")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("Date", "date"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "format",
                                                    new FunctionCall("getDateInstance", "DateFormat")
                                                            .addArgument("DateFormat.LONG")
                                                            .addArgument(new FunctionCall("getLocale", internalsVar))
                                            ).addArgument("date")
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToDate", "Date")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall("getDateFromYYMD", "Dates")
                                                    .addArguments("str", quickQuote("-"))
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Date", "date"))
                    .addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateDateFormat", "boolean")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new VarDeclaration(
                                            "SimpleInputDateFormat", "simpleInputDateFormat",
                                            new ObjectCreation("SimpleInputDateFormat")
                                                    .addArguments("SimpleInputDateFormat.ElementOrder.YYMD", quickQuote("-"))
                                    ).markAsFinal()
                            ).addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "validate",
                                                    "simpleInputDateFormat"
                                            ).addArgument("str")
                                    )
                            )
            ).addContent(EMPTY_LINE);
        }

        if (types.contains("Time")) {
            javaClass.addContent(
                    new FunctionDeclaration("formatTime", "String")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("Time", "time"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "format",
                                                    new FunctionCall("getTimeInstance", "DateFormat")
                                                            .addArgument("DateFormat.LONG")
                                                            .addArgument(new FunctionCall("getLocale", internalsVar))
                                            ).addArgument("time")
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToTime", "Time")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall("getTimeFromString", "Dates")
                                                    .addArguments("str", quickQuote(":"))
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Time", "time"))
                    .addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateTimeFormat", "boolean")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new VarDeclaration(
                                            "SimpleInputTimeFormat", "simpleInputTimeFormat",
                                            new ObjectCreation("SimpleInputTimeFormat").addArgument(quickQuote(":"))
                                    ).markAsFinal()
                            ).addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "validate",
                                                    "simpleInputTimeFormat"
                                            ).addArgument("str")
                                    )
                            )
            ).addContent(EMPTY_LINE);
        }

        if (types.contains("Timestamp")) {
            javaClass.addContent(
                    new FunctionDeclaration("formatTimestamp", "String")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("Timestamp", "timestamp"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "format",
                                                    new FunctionCall("getDateTimeInstance", "DateFormat")
                                                            .addArguments("DateFormat.LONG", "DateFormat.LONG")
                                                            .addArgument(new FunctionCall("getLocale", internalsVar))
                                            ).addArgument("timestamp")
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToTimestamp", "Timestamp")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new ReturnStatement(
                                            new FunctionCall("getTimestampFromYYMD", "Dates")
                                                    .addArguments("str", quickQuote("-"), quickQuote(":"))
                                    )
                            )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Timestamp", "timestamp"))
                    .addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateTimestampFormat", "boolean")
                            .visibility(Visibility.PROTECTED)
                            .addArgument(new FunctionArgument("String", "str"))
                            .addContent(
                                    new VarDeclaration(
                                            "SimpleInputTimestampFormat", "simpleInputTimestampFormat",
                                            new ObjectCreation("SimpleInputTimestampFormat")
                                                    .addArguments(
                                                            "SimpleInputDateFormat.ElementOrder.YYMD",
                                                            quickQuote("-"),
                                                            quickQuote(":"))
                                    ).markAsFinal()
                            ).addContent(
                                    new ReturnStatement(
                                            new FunctionCall(
                                                    "validate",
                                                    "simpleInputTimestampFormat"
                                            ).addArgument("str")
                                    )
                            )
            ).addContent(EMPTY_LINE);
        }
    }

    private void addGetAll() {
        javaClass.addContent(
                new FunctionDeclaration("getAll", "List<" + beanName + ">").markAsStatic().addContent(
                        new ReturnStatement(new FunctionCall("getAll").addArgument(new FunctionCall("getOrderByFields", parametersVar)))
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getAll", "List<" + beanName + ">").markAsStatic().visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "orderBy")).addContent(
                                new ReturnStatement(new FunctionCall("getSelection").addArguments("null", "orderBy", "null"))
                        )
        ).addContent(EMPTY_LINE);

        final ObjectCreation newBeanFromFields = new ObjectCreation(beanName);
        int index = 0;
        for (Column column: columns.getList()) {
            ++index;
            final String javaType = column.getJavaType();
            if (javaType.equals("Money"))
                newBeanFromFields
                        .addArgument(new ObjectCreation("Money")
                                .addArgument(new FunctionCall("getLong", "rs").addArgument(Integer.toString(index)))
                                .addArgument(new FunctionCall("getDefaultMoneyFormat", parametersVar)));
            else
                newBeanFromFields
                        .addArgument(new FunctionCall("get" + capitalize(javaType), "rs")
                                .addArguments(Integer.toString(index)));
        }
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                newBeanFromFields.addArgument(new FunctionCall("getSelection", relationship.getBeanClass())
                        .addArgument(new OperatorExpression(
                                quickQuote(relationship.getIdSqlName() + "="),
                                new FunctionCall("getLong", "rs").addArgument("1"),
                                OperatorExpression.Operator.ADD)));
        javaClass.addContent(
                new JavaClass("GetSelectionQueryProcess")
                        .implementsInterface("DBQueryRetrieveData<List<" + beanName + ">>")
                        .visibility(Visibility.PRIVATE)
                        .markAsStatic()
                        .addContent(
                                new FunctionDeclaration("processResultSet", "List<" + beanName + ">")
                                        .annotate("@Override")
                                        .addException("SQLException")
                                        .addArgument(new FunctionArgument("ResultSet", "rs"))
                                        .addContent(
                                                VarDeclaration.createListDeclaration(beanName, "list")
                                                        .markAsFinal()
                                        ).addContent(EMPTY_LINE).addContent(
                                                new WhileBlock(new Condition(new FunctionCall("next", "rs"))).addContent(
                                                        new FunctionCall("add", "list")
                                                                .byItself()
                                                                .addArgument(newBeanFromFields)
                                                )
                                        ).addContent(EMPTY_LINE).addContent(
                                                new ReturnStatement("list")
                                        )
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getSelection", "List<" + beanName + ">").markAsStatic().visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "whereClause")).addContent(
                                new ReturnStatement(new FunctionCall("getSelection").addArguments("whereClause", "null"))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getSelection", "List<" + beanName + ">").markAsStatic().visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "whereClause"))
                        .addArgument(new FunctionArgument("DBQuerySetup", "setup")).addContent(
                                new ReturnStatement(new FunctionCall("getSelection")
                                        .addArgument("whereClause")
                                        .addArguments(new FunctionCall("getOrderByFields", parametersVar))
                                        .addArgument("setup"))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getSelection", "List<" + beanName + ">").markAsStatic().visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "whereClause"))
                        .addArgument(new FunctionArgument("String", "orderBy"))
                        .addArgument(new FunctionArgument("DBQuerySetup", "setup")).addContent(
                                new IfBlock(new Condition(new Comparison("whereClause", "null"))
                                        .andCondition(new Condition(new Comparison("setup", "null", Comparison.Comparator.NEQ)))).addContent(
                                        new ExceptionThrow("IllegalArgumentException").addArgument(quickQuote("Cannot accept setup code without a WHERE clause."))
                                )
                        ).addContent(EMPTY_LINE).addContent(
                                new VarDeclaration("StringBuilder", "query", new ObjectCreation("StringBuilder")).markAsFinal()
                        ).addContent(
                                new FunctionCall("append", "query").addArgument(quickQuote(getAllFieldsQuery())).byItself()
                        ).addContent(
                                new IfBlock(new Condition(new Comparison("whereClause", "null", Comparison.Comparator.NEQ))).addContent(
                                        new FunctionCall("append", new FunctionCall("append", "query").addArgument(quickQuote(" WHERE "))).addArgument("whereClause").byItself()
                                )
                        ).addContent(
                                new IfBlock(new Condition(new Comparison("orderBy", "null", Comparison.Comparator.NEQ))).addContent(
                                        new FunctionCall("append", new FunctionCall("append", "query").addArgument(quickQuote(" ORDER BY "))).addArgument("orderBy").byItself()
                                )
                        ).addContent(EMPTY_LINE).addContent(
                                new IfBlock(new Condition(new Comparison("whereClause", "null"))
                                        .orCondition(new Condition(new Comparison("setup", "null")))).addContent(
                                        new ReturnStatement(new FunctionCall("processQuery", "dbAccess")
                                                .addArgument(new FunctionCall("toString", "query"))
                                                .addArgument(new ObjectCreation("GetSelectionQueryProcess")))
                                )
                        ).addContent(EMPTY_LINE).addContent(
                                new ReturnStatement(new FunctionCall("processQuery", "dbAccess")
                                        .addArgument(new FunctionCall("toString", "query"))
                                        .addArgument("setup")
                                        .addArgument(new ObjectCreation("GetSelectionQueryProcess")))
                        )
        ).addContent(EMPTY_LINE);


        javaClass.addContent(
                new JavaClass("GetSelectionCountQueryProcess")
                        .implementsInterface("DBQueryRetrieveData<Long>")
                        .visibility(Visibility.PRIVATE)
                        .markAsStatic()
                        .addContent(
                                new FunctionDeclaration("processResultSet", "Long")
                                        .annotate("@Override")
                                        .addException("SQLException")
                                        .addArgument(new FunctionArgument("ResultSet", "rs"))
                                        .addContent(
                                                new FunctionCall("next", "rs").byItself()
                                        )
                                        .addContent(
                                                new ReturnStatement(new FunctionCall("getLong", "rs").addArgument("1"))
                                        )
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getSelectionCount", "long")
                        .markAsStatic()
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "whereClause"))
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getSelectionCount").addArguments("whereClause", "null"))
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getSelectionCount", "long")
                        .markAsStatic()
                        .visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "whereClause"))
                        .addArgument(new FunctionArgument("DBQuerySetup", "setup"))
                        .addContent(
                                new VarDeclaration(
                                        "String",
                                        "query",
                                        quickQuote("SELECT COUNT(id) FROM " + tableName + " WHERE ") + " + whereClause"
                                ).markAsFinal()
                        ).addContent(EMPTY_LINE)
                        .addContent(
                                new IfBlock(new Condition(new Comparison("setup", "null")))
                                        .addContent(
                                                new ReturnStatement(new FunctionCall("processQuery", "dbAccess")
                                                        .addArgument("query")
                                                        .addArgument(new ObjectCreation("GetSelectionCountQueryProcess")))
                                        )
                        ).addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement(new FunctionCall("processQuery", "dbAccess")
                                        .addArgument("query")
                                        .addArgument("setup")
                                        .addArgument(new ObjectCreation("GetSelectionCountQueryProcess")))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addGetIdNamePairs() {
        javaClass.addContent(
                new FunctionDeclaration("getIdNamePairs", "List<IdNamePair>").markAsStatic().addArguments(
                        new FunctionArgument("List<String>", "dataFields"),
                        new FunctionArgument("List<String>", "orderingFields")
                ).addContent(
                        new ReturnStatement(
                                new FunctionCall("getIdNamePairs").addArguments("null", "dataFields", "orderingFields")
                        )
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getIdNamePairs", "List<IdNamePair>").visibility(Visibility.PROTECTED).markAsStatic().addArguments(
                        new FunctionArgument("String", "whereClause"),
                        new FunctionArgument("List<String>", "dataFields"),
                        new FunctionArgument("List<String>", "orderingFields")
                ).addContent(
                        new ReturnStatement(
                                new FunctionCall("getIdNamePairs", "DBQueries").addArguments("db", quickQuote(tableName), "whereClause", "dataFields", "orderingFields")
                        )
                )
        ).addContent(EMPTY_LINE);
    }

    private void addGetCount() {
        javaClass.addContent(
                new FunctionDeclaration("getCount", "long").markAsStatic().addContent(
                        new ReturnStatement(new FunctionCall("getLongCount", "DBQueries").addArguments("db", quickQuote(tableName)))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addIdOK() {
        javaClass.addContent(
                new FunctionDeclaration("isIdOK", "boolean")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("long", "id"))
                        .addContent(
                                new ReturnStatement(new FunctionCall("isIdOK", "DBQueries")
                                        .addArguments("db", quickQuote(tableName), "id"))
                        )
        ).addContent(EMPTY_LINE).addContent(
                new FunctionDeclaration("isIdOK", "boolean")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("long", "id"))
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addContent(
                                new ReturnStatement(new FunctionCall("isIdOK", "DBQueries")
                                        .addArguments("transaction", quickQuote(tableName), "id"))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addHumanReadableTitle() {
        javaClass.addContent(
                new FunctionDeclaration("getHumanReadableTitle", "String")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("long", "id"))
                        .addContent(
                                new IfBlock(new Condition("id == 0")).addContent(
                                        new ReturnStatement("\"\"")
                                )
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement(new FunctionCall("getHumanReadableTitle", "DBQueries")
                                        .addArguments("db", quickQuote(tableName), "id")
                                        .addArgument(new FunctionCall("getNamingFields", parametersVar)))
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addGetListFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getList", new GenericType("List", beanName))
                        .addArgument(new FunctionArgument("ResultSet", "rs"))
                        .markAsStatic()
                        .addContent(
                                VarDeclaration.createGenericContainerDeclaration(
                                        "List",
                                        "ArrayList",
                                        beanName,
                                        "list").markAsFinal()
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new TryBlock().addContent(
                                        new WhileBlock(new Condition(new FunctionCall("next", "rs"))).addContent(
                                                new FunctionCall("add", "list")
                                                        .addArgument(new ObjectCreation(beanName)
                                                                .addArgument("rs"))
                                                        .byItself()
                                        )
                                ).addCatchBlock(
                                        new CatchBlock(new FunctionArgument("SQLException", "sqlex"))
                                                .addContent(new ExceptionThrow("SQLRuntimeException").addArgument("sqlex")))
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement("list")
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addSetLocale() {
        javaClass.addContent(
                new FunctionDeclaration("setLocale")
                        .annotate("@Override")
                        .addArgument(new FunctionArgument("Locale", "locale"))
                        .addContent(
                                new FunctionCall("setLocale", internalsVar).addArgument("locale").byItself()
                        )
        ).addContent(EMPTY_LINE);
    }


    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addClassModifiers();
        addProperties();
        addConstructors();
        addSetIdFunction();
        addEquals();
        addToString();
        addSetters();
        addGetters();
        addLabelGetters();
        addRequiredIndicators();
        addUniqueIndicators();
        addOneToManyRelationshipManagement();
        addItemOrderManagement();
        addInitSetLabelFunctions();
        addUpdateDB();
        addDataOK();
        addReset();
        addDelete();
        addCreate();
        addUpdate();
        addUpdateLabels();
        addUpdateCaching();
        addOneToManyRelationshipInDB();
        addTemporalFunctions();
        addGetAll();
        addGetIdNamePairs();
        addGetCount();
        addIdOK();
        addHumanReadableTitle();
        addGetListFunction();
        addSetLocale();
    }

    private String getReadSQLQuery() {
        StringBuilder buf = new StringBuilder();

        buf.append("SELECT ");

        for (Column column: columns.getList()) {
            final String name = column.getSqlName();
            if (!name.equals("id")) {
                buf.append(backquote(name));
                buf.append(", ");
            }
        }
        buf.delete(buf.length() - 2, buf.length());

        buf.append(" FROM ");
        buf.append(tableName);
        buf.append(" WHERE id=?");

        return buf.toString();
    }

    private String getAllFieldsQuery() {
        StringBuilder buf = new StringBuilder();

        buf.append("SELECT ");

        for (Column column: columns.getList()) {
            final String name = column.getSqlName();
            buf.append(name);
            buf.append(", ");
        }
        buf.delete(buf.length() - 2, buf.length());

        buf.append(" FROM ");
        buf.append(tableName);

        return buf.toString();
    }

    private String getReadSQLQueryOneToManyRelationship(
            final OneToManyRelationship relationship,
            final String parametersFieldName)
    {
        return "\"SELECT \" + " + relationship.getBeanClass() + ".DATABASE_FIELD_LIST + \" FROM "
                + relationship.getTable()
                + " WHERE " + relationship.getIdSqlName() + "=? ORDER BY \" + "
                + parametersFieldName + ".getOrderByFields()";
    }

    private String getDeleteSQLQuery() {
        return "DELETE FROM " + tableName + " WHERE id=?";
    }

    private String getInsertSQLQuery() {
        StringBuilder buf = new StringBuilder();

        buf.append("INSERT INTO ");
        buf.append(tableName);
        buf.append(" (");

        int count = 0;
        for (Column column: columns.getList()) {
            final String name = column.getSqlName();
            if (!name.equals("id")) {
                count++;
                buf.append(backquote(name));
                buf.append(", ");
            }
        }
        buf.delete(buf.length() - 2, buf.length());

        buf.append(") VALUES (");

        for (int i = 0; i < count; i++)
            buf.append("?, ");
        buf.delete(buf.length() - 2, buf.length());

        buf.append(")");

        return buf.toString();
    }

    private String getUpdateSQLQuery() {
        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE ");
        buf.append(tableName);
        buf.append(" SET ");

        for (Column column: columns.getList()) {
            final String name = column.getSqlName();
            if (!name.equals("id")) {
                buf.append(backquote(name));
                buf.append("=?, ");
            }
        }
        buf.delete(buf.length() - 2, buf.length());

        buf.append(" WHERE id=?");

        if (columns.hasLastUpdate())
            buf.append(" AND last_update=?");

        return buf.toString();
    }

    private String getDeleteOneToManyRelationshipQuery(final String tableName, final String indexField) {
        return "DELETE FROM " + tableName + " WHERE " + indexField + "=?";
    }

    private FunctionDeclaration getTemporalConvertToStringFunction(final String className, final String varName) {
        return new FunctionDeclaration("convert" + className + "ToString", "String").addArgument(new FunctionArgument(className, varName)).addContent(
                new IfBlock(new Condition(new Comparison(varName, "null"))).addContent(
                        new ReturnStatement(EMPTY_STRING)
                )
        ).addContent(EMPTY_LINE).addContent(
                new ReturnStatement(new FunctionCall("toString", varName))
        ).visibility(Visibility.PROTECTED);
    }

    private void addIndicator(final String javaName, final String indicatorName, final boolean value, final boolean isFinal) {
        final FunctionDeclaration indicator = new FunctionDeclaration("is" + capitalize(javaName) + indicatorName, "boolean");
        if (isFinal)
            indicator.markAsFinal();
        if (value)
            indicator.addContent(new ReturnStatement("true"));
        else
            indicator.addContent(new ReturnStatement("false"));
        javaClass.addContent(indicator).addContent(EMPTY_LINE);
    }

    private String backquote(String fieldName) {
        return "`" + fieldName + "`";
    }
}

