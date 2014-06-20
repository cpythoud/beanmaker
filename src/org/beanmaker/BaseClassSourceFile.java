package org.beanmaker;

import java.util.List;
import java.util.Set;

import org.jcodegen.java.AnonymousClassCreation;
import org.jcodegen.java.Assignment;
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
import org.jcodegen.java.LineOfCode;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.OperatorExpression;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;
import org.jcodegen.java.WhileBlock;

import org.dbbeans.util.Strings;

import static org.dbbeans.util.Strings.camelize;
import static org.dbbeans.util.Strings.capitalize;
import static org.dbbeans.util.Strings.quickQuote;
import static org.dbbeans.util.Strings.uncapitalize;

import static org.beanmaker.SourceFiles.chopId;

public class BaseClassSourceFile extends BeanCodeWithDBInfo {

    private final Set<String> types;

    private final String internalsVar;
    private final String parametersVar;

	public BaseClassSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "Base", columns, tableName);

        internalsVar = beanVarName + "Internals";
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
        importsManager.addImport("org.beanmaker.util.DBQueries");
        importsManager.addImport("org.beanmaker.util.ErrorMessage");
        importsManager.addImport("org.beanmaker.util.IdNamePair");

        importsManager.addImport("org.dbbeans.sql.DBQueryProcess");
        importsManager.addImport("org.dbbeans.sql.DBQuerySetup");
        importsManager.addImport("org.dbbeans.sql.DBQuerySetupProcess");
        importsManager.addImport("org.dbbeans.sql.DBTransaction");

        importsManager.addImport("org.dbbeans.util.Strings");

        if (columns.containNumericalData())
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
    }
	
	private void addClassModifiers() {
        javaClass.markAsAbstract().extendsClass("DbBean");
	}

    private void addProperties() {
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            final VarDeclaration declaration;

            if (type.equals("String"))
                declaration = new VarDeclaration("String", field, EMPTY_STRING);
            else if (type.equals("Money"))
                declaration = new VarDeclaration("Money", field, new ObjectCreation("Money").addArgument("0").addArgument(new FunctionCall("getDefaultMoneyFormat")));
            else
                declaration = new VarDeclaration(type, field);

            addProperty(declaration);

            if (type.equals("Money"))
                addProperty(new VarDeclaration("String", field + "Str", new FunctionCall("toString", field)));
            if (JAVA_TEMPORAL_TYPES.contains(type) || ((type.equals("int") || type.equals("long")) && !field.startsWith("id") && !field.equals("itemOrder")))
                addProperty(new VarDeclaration("String", field + "Str", EMPTY_STRING));
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
        javaClass.addContent(
                new VarDeclaration("BeanInternals", internalsVar,
                        new ObjectCreation("BeanInternals").addArgument(quickQuote(bundleName))).markAsFinal().visibility(Visibility.PROTECTED)
        );
        final String parametersClass = beanName + "Parameters";
        javaClass.addContent(
                new VarDeclaration(parametersClass, parametersVar,
                        new ObjectCreation(parametersClass)).markAsFinal().markAsStatic().visibility(Visibility.PROTECTED)
        );
        newLine();
    }
	
	private void addConstructors() {
        javaClass.addContent(getBaseConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getIdArgumentConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getCopyConstructor()).addContent(EMPTY_LINE);
	}

    private ConstructorDeclaration getBaseConstructor() {
        return javaClass.createConstructor();
    }

    private ConstructorDeclaration getIdArgumentConstructor() {
        return getBaseConstructor().addArgument(new FunctionArgument("long", "id")).addContent("setId(id);");
    }

    private ConstructorDeclaration getCopyConstructor() {
        final ConstructorDeclaration copyConstructor = getBaseConstructor();
        copyConstructor.addArgument(new FunctionArgument(beanName + "Base", "model")).addContent(new Assignment("id", "0"));
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!field.equals("id")) {
                if (field.equals("itemOrder"))
                    copyConstructor.addContent(new Assignment("itemOrder", "0"));
                else if (field.startsWith("id") || type.equals("boolean") || type.equals("String"))
                    copyConstructor.addContent(new Assignment(field, "model." + field));
                else
                    copyConstructor.addContent(new FunctionCall("set" + capitalize(field)).addArgument("model." + field).byItself());
            }
        }

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly()) {
                final String beanClass = relationship.getBeanClass();
                final String beanObject = uncapitalize(beanClass);
                final String javaName = relationship.getJavaName();
                copyConstructor.addContent(EMPTY_LINE).addContent(
                        new ForLoop(beanClass + " " + beanObject + ": model." + javaName).addContent(
                                new FunctionCall("add", javaName).byItself().addArgument(
                                        new ObjectCreation(beanClass).addArgument(new FunctionCall("getId", beanObject))
                                )
                        )
                );
            }

        return copyConstructor;
    }
	
	private void addSetIdFunction() {
		final List<OneToManyRelationship> relationships = columns.getOneToManyRelationships();

        final FunctionDeclaration function = new FunctionDeclaration("setId")
                .addArgument(new FunctionArgument("long", "id"));

        // function inner class for database row retrieval
        final JavaClass databaseInnerClass = new JavaClass("DataFromDBQuery").visibility(Visibility.NONE)
                .implementsInterface("DBQuerySetupProcess");
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!field.equals("id")) {
                if (type.equals("int") || type.equals("long"))
                    databaseInnerClass.addContent(new VarDeclaration(type, field, "0"));
                else if (type.equals("String") || JAVA_TEMPORAL_TYPES.contains(type) || type.equals("Money"))
                    databaseInnerClass.addContent(new VarDeclaration(type, field, "null"));
                else if (type.equals("boolean"))
                    databaseInnerClass.addContent(new VarDeclaration(type, field, "false"));
                else
                    throw new IllegalStateException("Java type not allowed: " + type);
            }
        }
        databaseInnerClass.addContent(new VarDeclaration("boolean", "idOK", "false"))
                .addContent(EMPTY_LINE)
                .addContent(getInnerClassSetupPSWithIdFunction())
                .addContent(EMPTY_LINE);
        final FunctionDeclaration processRS = getInnerClassProcessRSFunctionStart();
        final IfBlock ifRsNext = new IfBlock(new Condition("rs.next()"));
        int index = 0;
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!field.equals("id")) {
                ++index;
                if (type.equals("Money"))
                    ifRsNext.addContent(new Assignment(field, new ObjectCreation("Money")
                            .addArgument(new FunctionCall("getLong")
                                    .addArgument(Integer.toString(index))
                                    .addArgument(new FunctionCall("getDefaultMoneyFormat")))));
                else {
                    final String getterName = "get" + capitalize(type);
                    ifRsNext.addContent(new Assignment(field, new FunctionCall(getterName, "rs").addArgument(Integer.toString(index))));
                }
            }
        }
        ifRsNext.addContent(new Assignment("idOK", "true"));
        processRS.addContent(ifRsNext);
        databaseInnerClass.addContent(processRS);
        function.addContent(databaseInnerClass).addContent(EMPTY_LINE);

        // for objects containing one or more list of other kind of objects
        for (OneToManyRelationship relationship: relationships) {
            if (!relationship.isListOnly()) {
                final JavaClass extraDBInnerClass = new JavaClass("DataFromDBQuery" + capitalize(relationship.getJavaName()))
                        .visibility(Visibility.NONE).implementsInterface("DBQuerySetupProcess")
                        .addContent(VarDeclaration.createListDeclaration(relationship.getBeanClass(), relationship.getJavaName()).markAsFinal())
                        .addContent(EMPTY_LINE)
                        .addContent(getInnerClassSetupPSWithIdFunction())
                        .addContent(EMPTY_LINE);
                final FunctionDeclaration processRSExtra = getInnerClassProcessRSFunctionStart()
                        .addContent(new WhileBlock(new Condition("rs.next()"))
                                .addContent(new FunctionCall("add", relationship.getJavaName())
                                        .byItself()
                                        .addArgument(new ObjectCreation(relationship.getBeanClass())
                                                .addArgument("rs.getLong(1)"))));
                extraDBInnerClass.addContent(processRSExtra);
                function.addContent(extraDBInnerClass).addContent(EMPTY_LINE);
            }
        }

        // check for bad ID
        function.addContent(new IfBlock(new Condition("id <= 0")).addContent("throw new IllegalArgumentException(\"id = \" + id + \" <= 0\");"))
                .addContent(EMPTY_LINE);

        // instantiate DBQuery inner class & use it to retrieve data
        function.addContent(
                new VarDeclaration("DataFromDBQuery", "dataFromDBQuery", new ObjectCreation("DataFromDBQuery")).markAsFinal()
        ).addContent(
                new FunctionCall("processQuery", "dbAccess")
                        .byItself()
                        .addArgument(quickQuote(getReadSQLQuery()))
                        .addArgument("dataFromDBQuery")
        ).addContent(EMPTY_LINE);

        // check if data was returned
        function.addContent(
                new IfBlock(new Condition("!dataFromDBQuery.idOK")).addContent(
                        new ExceptionThrow("IllegalArgumentException").addArgument("\"id = \" + id + \" does not exist\"")
                )
        ).addContent(EMPTY_LINE);

        // for objects containing one or more list of other kind of objects
        for (OneToManyRelationship relationship: relationships) {
            if (!relationship.isListOnly()) {
                final String cappedJavaName = capitalize(relationship.getJavaName());
                function.addContent(
                        new VarDeclaration("DataFromDBQuery" + cappedJavaName, "dataFromDBQuery" + cappedJavaName, new ObjectCreation("DataFromDBQuery" + cappedJavaName)).markAsFinal()
                ).addContent(
                        new FunctionCall("processQuery", "dbAccess")
                                .byItself()
                                .addArgument(quickQuote(getReadSQLQueryOneToManyRelationship(relationship.getTable(), relationship.getIdSqlName())))
                                .addArgument("dataFromDBQuery" + cappedJavaName)
                ).addContent(EMPTY_LINE);
            }
        }

        // extra DB actions
        function.addContent("initExtraDbActions(id);").addContent(EMPTY_LINE);

        // fields assignment
        function.addContent(new Assignment("this.id", "id"));
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!field.equals("id")) {
                function.addContent(new Assignment("this." + field, "dataFromDBQuery." + field));
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    function.addContent(
                            new Assignment(field +"Str",
                                    new FunctionCall("convert" + type + "ToString").addArgument(field))
                    );
                if ((type.equals("int") || type.equals("long")) && !field.equals("itemOrder") && !field.startsWith("id"))
                    function.addContent(
                            new Assignment(field +"Str",
                                    new FunctionCall("valueOf", "String").addArgument(field))
                    );
                if (type.equals("Money"))
                    function.addContent(
                            new Assignment(field +"Str",
                                    new FunctionCall("toString", field))
                    );
            }
        }

        for (OneToManyRelationship relationship: relationships)
            if (!relationship.isListOnly())
                function.addContent(
                        new Assignment("this." + relationship.getJavaName(), "dataFromDBQuery" + capitalize(relationship.getJavaName()) + "." + relationship.getJavaName())
                );

        function.addContent(EMPTY_LINE).addContent("postInitActions();");
        javaClass.addContent(function).addContent(EMPTY_LINE);


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
                new FunctionDeclaration("resetId")
                        .addContent("id = 0;")
        ).addContent(EMPTY_LINE);
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
	
	private void addSetters() {
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!field.equals("id") && !field.equals("lastUpdate") && !field.equals("modifiedBy") && !field.equals("itemOrder")) {
                final FunctionDeclaration setter = new FunctionDeclaration("set" + capitalize(field))
                        .addArgument(new FunctionArgument(type, field));
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    setter.addContent(
                            new Assignment("this." + field,
                                    new ObjectCreation(type)
                                            .addArgument(new FunctionCall("getTime", field)))
                    );
                else
                    setter.addContent(
                            new Assignment("this." + field, field)
                    );
                if (type.equals("int") && !field.startsWith("id"))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", "Integer").addArgument(field))
                    );
                if (type.equals("long") && !field.startsWith("id"))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", "Long").addArgument(field))
                    );
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("convert" + capitalize(type) + "ToString").addArgument(field))
                    );
                if (type.equals("Money"))
                    setter.addContent(
                            new Assignment(field + "Str", new FunctionCall("toString", field))
                    );
                javaClass.addContent(setter).addContent(EMPTY_LINE);

                if (column.couldHaveAssociatedBean() && column.hasAssociatedBean()) {
                    final String associatedBeanClass = column.getAssociatedBeanClass();
                    final String associatedBeanObject = uncapitalize(chopId(field));
                    final FunctionDeclaration fromObjectSetter = new FunctionDeclaration("set" + capitalize(associatedBeanObject))
                            .addArgument(new FunctionArgument(associatedBeanClass, associatedBeanObject))
                            .addContent(new IfBlock(new Condition(new Comparison(new FunctionCall("getId", associatedBeanObject), "0")))
                                    .addContent(new ExceptionThrow("IllegalArgumentException")
                                            .addArgument(quickQuote("Cannot accept uninitialized " + associatedBeanClass + " bean (id = 0) as argument."))))
                            .addContent(EMPTY_LINE)
                            .addContent(new Assignment(field, new FunctionCall("getId", associatedBeanObject)));
                    javaClass.addContent(fromObjectSetter).addContent(EMPTY_LINE);
                }

                if (JAVA_TEMPORAL_TYPES.contains(type) || type.equals("Money") || ((type.equals("int") || type.equals("long")) && !field.startsWith("id"))) {
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

            final String prefix;
            if (type.equals("boolean"))
                prefix = "is";
            else
                prefix = "get";
            final FunctionDeclaration getter = new FunctionDeclaration(prefix + capitalize(field), type);

            if (JAVA_TEMPORAL_TYPES.contains(type))
                getter.addContent(
                        new IfBlock(new Condition(new Comparison(field, "null"))).addContent(new ReturnStatement("null"))
                ).addContent(
                        EMPTY_LINE
                ).addContent(
                        new ReturnStatement(new ObjectCreation(type).addArgument(new FunctionCall("getTime", field)))
                );
            else
                getter.addContent(
                        new ReturnStatement(field)
                );

            javaClass.addContent(getter).addContent(EMPTY_LINE);


            if (column.hasAssociatedBean()) {
                final String associatedBeanClass = column.getAssociatedBeanClass();
                final FunctionDeclaration associatedBeanGetter = new FunctionDeclaration("get" + chopId(field), associatedBeanClass)
                        .addContent(new ReturnStatement(new ObjectCreation(associatedBeanClass).addArgument(field)));
                javaClass.addContent(associatedBeanGetter).addContent(EMPTY_LINE);
            }

            if (JAVA_TEMPORAL_TYPES.contains(type) || type.equals("Money") || ((type.equals("int") || type.equals("long")) && !field.startsWith("id") && !field.equals("itemOrder"))) {
                final FunctionDeclaration strGetter = new FunctionDeclaration("get" + capitalize(field) + "Str", "String")
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
            if (!column.isSpecial())
                javaClass.addContent(
                        new FunctionDeclaration("get" + capitalize(field) + "Label", "String").addContent(
                                new ReturnStatement(new FunctionCall("getLabel", internalsVar).addArgument(quickQuote(field)))
                        )
                ).addContent(EMPTY_LINE);
        }
    }

    private ExceptionThrow getCannotDisplayBadDataException() {
        return new ExceptionThrow("IllegalArgumentException").addArgument(quickQuote("Cannot display bad data"));
    }

    private void addRequiredIndicators() {
        for (Column column: columns.getList())
            addIndicator(column.getJavaName(), "Required", column.isRequired(), true);
    }

    private void addUniqueIndicators() {
        for (Column column: columns.getList())
            addIndicator(column.getJavaName(), "ToBeUnique", column.isUnique(), true);
    }

	private void addOneToManyRelationshipManagement() {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            final String beanClass = relationship.getBeanClass();
            final String itemName = uncapitalize(beanClass);
            final String listName = relationship.getJavaName();

            if (relationship.isListOnly()) {
                importsManager.addImport("org.dbbeans.sql.DBQuerySetupRetrieveData");

                final FunctionDeclaration preparedStatementSetup = new FunctionDeclaration("setupPreparedStatement")
                        .annotate("@Override")
                        .addException("SQLException")
                        .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                        .addContent(new FunctionCall("setLong", "stat")
                                .addArgument("1")
                                .addArgument("id")
                                .byItself());

                final FunctionCall dbAccessFunction = new FunctionCall("processQuery", "dbAccess").byItself();
                final FunctionDeclaration listGetter =
                        new FunctionDeclaration("get" + capitalize(listName), new GenericType("List", beanClass))
                                .addContent(VarDeclaration.createListDeclaration(beanClass, listName).markAsFinal())
                                .addContent(EMPTY_LINE)
                                .addContent(dbAccessFunction
                                        .addArgument(new OperatorExpression(quickQuote("SELECT id FROM " + relationship.getTable() + " WHERE " + relationship.getIdSqlName() + "=? ORDER BY "),
                                                new FunctionCall("getOrderByFields", beanClass + "." + Strings.uncamelize(beanClass).toUpperCase() + "_PARAMETERS"),
                                                OperatorExpression.Operator.ADD))
                                        .addArgument(new AnonymousClassCreation("DBQuerySetupProcess").setContext(dbAccessFunction)
                                                .addContent(preparedStatementSetup)
                                                .addContent(EMPTY_LINE)
                                                .addContent(new FunctionDeclaration("processResultSet")
                                                        .annotate("@Override")
                                                        .addException("SQLException")
                                                        .addArgument(new FunctionArgument("ResultSet", "rs"))
                                                        .addContent(new WhileBlock(new Condition("rs.next()"))
                                                                .addContent(new FunctionCall("add", listName).byItself()
                                                                        .addArgument(new ObjectCreation(beanClass)
                                                                                .addArgument(new FunctionCall("getLong", "rs")
                                                                                        .addArgument("1"))))))))
                                .addContent(EMPTY_LINE)
                                .addContent(new ReturnStatement(listName));
                javaClass.addContent(listGetter).addContent(EMPTY_LINE);

                final FunctionCall dbAccessForCountFunction = new FunctionCall("processQuery", "dbAccess");
                final FunctionDeclaration countGetter =
                        new FunctionDeclaration("getCountFor" + capitalize(listName), "long")
                                .addContent(new ReturnStatement(
                                        dbAccessForCountFunction
                                                .addArgument(quickQuote("SELECT COUNT(id) FROM " + relationship.getTable() + " WHERE " + relationship.getIdSqlName() + "=?"))
                                                .addArgument(new AnonymousClassCreation("DBQuerySetupRetrieveData<Long>").setContext(dbAccessFunction)
                                                        .addContent(preparedStatementSetup)
                                                        .addContent(new FunctionDeclaration("processResultSet", "Long")
                                                                .annotate("@Override")
                                                                .addException("SQLException")
                                                                .addArgument(new FunctionArgument("ResultSet", "rs"))
                                                                .addContent(new FunctionCall("next", "rs").byItself())
                                                                .addContent(new ReturnStatement(new FunctionCall("getLong", "rs").addArgument("1")))))
                                ));
                javaClass.addContent(countGetter).addContent(EMPTY_LINE);
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
                new FunctionDeclaration("isFirstItemOrder", "boolean").addContent(
                       checkForItemOrderOperationOnUninitializedBean()
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("itemOrder == 1")
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration isLastItemOrderFunction = new FunctionDeclaration("isLastItemOrder", "boolean").addContent(
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
                new FunctionDeclaration("itemOrderMoveUp").addContent(
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
                new FunctionDeclaration("itemOrderMoveDown").addContent(
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
                    new FunctionDeclaration("itemOrderMoveAfter")
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
                    new FunctionDeclaration("itemOrderMoveBefore")
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
                    new FunctionDeclaration("itemOrderMoveAfter").addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
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
                    new FunctionDeclaration("itemOrderMoveBefore").addArgument(new FunctionArgument(beanName, beanVarName)).addContent(
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
    
    private static FunctionCall getPreUpdateConversionCall() {
        return new FunctionCall("preUpdateConversions").byItself();
    }
	
	private void addUpdateDB() {
        if (columns.hasLastUpdate())
            javaClass.addContent(
                    new FunctionDeclaration("isUpdateOK", "boolean").addContent(
                            new ReturnStatement("updateOK")
                    )
            ).addContent(EMPTY_LINE);

        final FunctionDeclaration updateDBFunction = new FunctionDeclaration("updateDB");
        if (columns.hasModifiedBy())
            updateDBFunction.addArgument(new FunctionArgument("String", "username"));

        updateDBFunction.addContent(
                getPreUpdateConversionCall()
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


        final FunctionDeclaration updateDBFunctionWithTransaction = new FunctionDeclaration("updateDB", "long").addArgument(new FunctionArgument("DBTransaction", "transaction"));
        if (columns.hasModifiedBy())
            updateDBFunctionWithTransaction.addArgument(new FunctionArgument("String", "username"));

        updateDBFunctionWithTransaction.addContent(
                getPreUpdateConversionCall()
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


        final FunctionDeclaration preUpdateConversionsFunction = new FunctionDeclaration("preUpdateConversions");
        preUpdateConversionsFunction.addContent(
                ifNotDataOK().addContent(
                        ExceptionThrow.getThrowExpression("IllegalArgumentException", "BAD DATA")
                )
        ).addContent(EMPTY_LINE);
        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                if ((type.equals("int") || type.equals("long")) && !field.startsWith("id"))
                    preUpdateConversionsFunction.addContent(
                            new Assignment(field,
                                    new FunctionCall("Strings.get" + capitalize(type) + "Val").addArgument(field + "Str"))
                    );
                if (JAVA_TEMPORAL_TYPES.contains(type))
                    preUpdateConversionsFunction.addContent(
                            new IfBlock(new Condition(new FunctionCall("isEmpty", "Strings").addArgument(field + "Str"), true)).addContent(
                                    new Assignment(field, new FunctionCall("convertStringTo" + type).addArgument(field + "Str"))
                            ).elseClause(new ElseBlock().addContent(
                                    new Assignment(field, "null")
                            ))
                    );
                if (type.equals("Money"))
                    preUpdateConversionsFunction.addContent(
                            new Assignment(field, new ObjectCreation("Money").addArgument(field + "Str").addArgument(new FunctionCall("getDefaultMoneyFormat")))
                    );
            }
        }

        javaClass.addContent(preUpdateConversionsFunction).addContent(EMPTY_LINE);
	}

    private void addDataOK() {
        final FunctionDeclaration dataOKFunction = new FunctionDeclaration("isDataOK", "boolean").addContent(
                new FunctionCall("clearErrorMessages", internalsVar).byItself()
        ).addContent(
                new VarDeclaration("boolean", "ok", "true")
        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {

                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);

                final IfBlock checkRequired = new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "Required"))).addContent(
                        new FunctionCall("addErrorMessage", internalsVar).byItself()
                                .addArgument("id")
                                .addArgument(quickQuote(field))
                                .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                .addArgument(new FunctionCall("get" + fieldCap + "EmptyErrorMessage"))
                ).addContent(new Assignment("ok", "false"));

                final IfBlock checkOKAndUnique = new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "OK"), true)).addContent(
                        new FunctionCall("addErrorMessage", internalsVar).byItself()
                                .addArgument("id")
                                .addArgument(quickQuote(field))
                                .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                .addArgument(new FunctionCall("get" + fieldCap + "BadFormatErrorMessage"))
                ).addContent(new Assignment("ok", "false"));

                if (column.isUnique())
                    checkOKAndUnique.elseClause(new ElseBlock().addContent(
                            new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "Unique"), true)).addContent(
                                    new FunctionCall("addErrorMessage", internalsVar).byItself()
                                            .addArgument("id")
                                            .addArgument(quickQuote(field))
                                            .addArgument(new FunctionCall("get" + fieldCap + "Label"))
                                            .addArgument(new FunctionCall("get" + fieldCap + "NotUniqueErrorMessage"))
                            ).addContent(new Assignment("ok", "false"))
                    ));

                dataOKFunction.addContent(
                        new IfBlock(new Condition(new FunctionCall("is" + fieldCap + "Empty"))).addContent(checkRequired)
                                .elseClause(new ElseBlock().addContent(checkOKAndUnique))
                ).addContent(EMPTY_LINE);
            }
        }

        dataOKFunction.addContent(
                new ReturnStatement("ok")
        );

        javaClass.addContent(dataOKFunction).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);

                final ReturnStatement returnStatement;
                if ((type.equals("int") || type.equals("long")) && field.startsWith("id"))
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

        for (Column column: columns.getList()) {
            if (!column.isSpecial() && !column.getJavaType().equals("boolean")) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final String fieldCap = capitalize(field);

                final FunctionDeclaration isOKFunction = new FunctionDeclaration("is" + fieldCap + "OK", "boolean");

                if ((type.equals("int") || type.equals("long"))) {
                    if (field.startsWith("id"))
                        isOKFunction.addContent(new ReturnStatement(new FunctionCall("isIdOK", column.getAssociatedBeanClass()).addArgument(field)));
                    else {
                        importsManager.addImport("org.beanmaker.util.FormatCheckHelper");
                        isOKFunction.addContent(new ReturnStatement(new FunctionCall("isNumber", "FormatCheckHelper").addArgument(field + "Str")));
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
                    isOKFunction.addContent(new ReturnStatement(new FunctionCall("isValOK", "getDefaultMoneyFormat()").addArgument(field + "Str")));
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
            if (!column.isSpecial() && column.isUnique()) {
                importsManager.addImport("org.dbbeans.sql.queries.BooleanCheckQuery");
                final FunctionCall dbAccessFunctionCall = new FunctionCall("processQuery", "!dbAccess");
                javaClass.addContent(
                        new FunctionDeclaration("is" + capitalize(column.getJavaName() + "Unique"), "boolean").addContent(  // TODO: IMPLEMENT!!!
                                new ReturnStatement(
                                        dbAccessFunctionCall
                                                .addArgument(Strings.quickQuote(getNotUniqueQuery(column)))
                                                .addArgument(new AnonymousClassCreation("BooleanCheckQuery").setContext(dbAccessFunctionCall, 1).addContent(
                                                        new FunctionDeclaration("setupPreparedStatement")
                                                                .addArgument(new FunctionArgument("PreparedStatement", "stat"))
                                                                .annotate("@Override").addException("SQLException").addContent(
                                                                new FunctionCall("set" + capitalize(column.getJavaType()), "stat")
                                                                        .addArgument("1").addArgument(column.getJavaName()).byItself()
                                                        ).addContent(
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
            if (!column.isSpecial() && column.isUnique()) {
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
                new FunctionDeclaration("getErrorMessages", "List<ErrorMessage>").addContent(
                        new ReturnStatement(new FunctionCall("getErrorMessages", internalsVar))
                )
        );
    }

    private String getNotUniqueQuery(final Column column) {
        return "SELECT id FROM " + tableName + " WHERE " + column.getSqlName() + "=? AND id <> ?";
    }
	
	private void addReset() {
        final FunctionDeclaration resetFunction = new FunctionDeclaration("reset");

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();

                if (type.equals("boolean"))
                    resetFunction.addContent(new Assignment(field, "false"));
                else if (type.equals("int") || type.equals("long"))
                    resetFunction.addContent(new Assignment(field, "0"));
                else if (type.equals("String"))
                    resetFunction.addContent(new Assignment(field, EMPTY_STRING));
                else if (type.equals("Money"))
                    resetFunction.addContent(new Assignment(field, new ObjectCreation("Money").addArgument("0").addArgument(new FunctionCall("getDefaultMoneyFormat"))));
                else
                    resetFunction.addContent(new Assignment(field, "null"));

                if (JAVA_TEMPORAL_TYPES.contains(type) || type.equals("Money") || ((type.equals("int") || type.equals("int")) && !field.startsWith("id")))
                    resetFunction.addContent(new Assignment(field + "Str", EMPTY_STRING));
            }
        }

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                resetFunction.addContent(new FunctionCall("clear", relationship.getJavaName()).byItself());

		
		resetFunction.addContent(new FunctionCall("clearErrorMessages", internalsVar).byItself());

        javaClass.addContent(resetFunction).addContent(EMPTY_LINE);


		final FunctionDeclaration fullResetFunction = new FunctionDeclaration("fullReset").addContent(
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

        javaClass.addContent(fullResetFunction).addContent(EMPTY_LINE);
	}
	
	private void addDelete() {
        final FunctionDeclaration deleteFunction = new FunctionDeclaration("delete");
        final FunctionCall accessDB = new FunctionCall("addUpdate", "transaction").byItself();

        deleteFunction.addContent(
                new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal()
        );

        if (columns.hasItemOrder())
            deleteFunction.addContent(
                    new VarDeclaration("long", "curItemOrder").markAsFinal()
            ).addContent(
                    new IfBlock(new Condition(new FunctionCall("isLastItemOrder"))).addContent(
                            new Assignment("curItemOrder", "0")
                    ).elseClause(new ElseBlock().addContent(
                            new Assignment("curItemOrder", "itemOrder")
                    ))
            );

        deleteFunction.addContent(
                accessDB.addArgument(quickQuote(getDeleteSQLQuery()))
                        .addArgument(new AnonymousClassCreation("DBQuerySetup").setContext(accessDB).addContent(
                        new FunctionDeclaration("setupPreparedStatement").annotate("@Override").addException("SQLException")
                                .addArgument(new FunctionArgument("PreparedStatement", "stat")).addContent(
                                new FunctionCall("setLong", "stat").addArguments("1", "id").byItself()
                        )
                ))
        );

        if (columns.hasItemOrder()) {
            final IfBlock checkItemOrderNotMax = new IfBlock(new Condition(new Comparison("curItemOrder", "0", Comparison.Comparator.GREATER_THAN)));

            final Column itemOrderField = columns.getItemOrderField();
            if (itemOrderField.isUnique())
                checkItemOrderNotMax.addContent(
                        getUpdateItemOrderAboveFunctionCall("getUpdateItemOrdersAboveQuery", null)
                );
            else {
                final String itemOrderAssociatedField = uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField()));
                checkItemOrderNotMax.addContent(
                        new IfBlock(new Condition(new Comparison(itemOrderAssociatedField, "0"))).addContent(
                                getUpdateItemOrderAboveFunctionCall("getUpdateItemOrdersAboveQueryWithNullSecondaryField", null)
                        ).elseClause(new ElseBlock().addContent(
                                getUpdateItemOrderAboveFunctionCall("getUpdateItemOrdersAboveQuery", itemOrderAssociatedField)
                        )));
            }

            deleteFunction.addContent(checkItemOrderNotMax);
        }

        deleteFunction.addContent(
                new FunctionCall("deleteExtraDbActions").byItself().addArgument("transaction")
        ).addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(
                EMPTY_LINE
        ).addContent(
                new FunctionCall("postDeleteActions").byItself()
        ).addContent(
                new FunctionCall("fullReset").byItself()
        );

        javaClass.addContent(deleteFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("deleteExtraDbActions").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postDeleteActions").visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
	}

    private FunctionCall getUpdateItemOrderAboveFunctionCall(final String queryRetrievalFunction, final String itemOrderAssociatedField) {
        final FunctionCall functionCall = new FunctionCall("updateItemOrdersAbove", "DBQueries").byItself()
                .addArgument(new FunctionCall(queryRetrievalFunction, parametersVar))
                .addArgument("transaction")
                .addArgument("curItemOrder");

        if (itemOrderAssociatedField != null)
            functionCall.addArgument(itemOrderAssociatedField);

        return functionCall;
    }

    private FunctionCall getStatSetFunction(final String type, final String field, final int index) {
        return new FunctionCall("set" + capitalize(type), "stat").byItself().addArguments(Integer.toString(index), field);
    }

    private JavaCodeBlock getFieldCreationOrUpdate(final Column column, final int index) {
        final String type = column.getJavaType();
        final String field = column.getJavaName();

        if (column.isRequired())
            return getStatSetFunction(type, field, index);

        if (column.hasAssociatedBean())
            return new IfBlock(new Condition(new Comparison(field, "0"))).addContent(
                    new FunctionCall("setNull", "stat").byItself().addArguments(Integer.toString(index), "java.sql.Types.INTEGER")
            ).elseClause(new ElseBlock().addContent(
                    getStatSetFunction(type, field, index)
            ));

        if (JAVA_TEMPORAL_TYPES.contains(type))
            return new IfBlock(new Condition(new Comparison(field, "null"))).addContent(
                    new FunctionCall("setNull", "stat").byItself().addArguments(Integer.toString(index), "java.sql.Types." + type.toUpperCase())
            ).elseClause(new ElseBlock().addContent(
                    getStatSetFunction(type, field, index)
            ));

        return getStatSetFunction(type, field, index);
    }

    private void addCreate() {
        int index = 0;

        final JavaClass recordCreationSetupClass = new JavaClass("RecordCreationSetup").implementsInterface("DBQuerySetup").visibility(Visibility.PRIVATE);
        final FunctionDeclaration setupStatFunction = new FunctionDeclaration("setupPreparedStatement").annotate("@Override")
                .addException("SQLException").addArgument(new FunctionArgument("PreparedStatement", "stat"));
        for (Column column: columns.getList()) {
            final String type = column.getJavaType();
            final String field = column.getJavaName();
            if (!column.isId()) {
                if (type.equals("Money")) {
                    final String suggestedType = Column.getSuggestedType(column.getSqlTypeName(), column.getPrecision());
                    if (suggestedType.equals("int"))
                        setupStatFunction.addContent(
                                new FunctionCall("setInt", "stat").byItself().addArgument(Integer.toString(++index)).addArgument(new FunctionCall("getIntVal", field))
                        );
                    else if (suggestedType.equals("long"))
                        setupStatFunction.addContent(
                                new FunctionCall("setLong", "stat").byItself().addArgument(Integer.toString(++index)).addArgument(new FunctionCall("getVal", field))
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
                new JavaClass("RecordUpdateSetup").extendsClass("RecordCreationSetup").visibility(Visibility.PRIVATE).addContent(
                        new FunctionDeclaration("setupPreparedStatement").annotate("@Override")
                                .addException("SQLException").addArgument(new FunctionArgument("PreparedStatement", "stat")).addContent(
                                new FunctionCall("setupPreparedStatement", "super").byItself().addArgument("stat")
                        ).addContent(
                                new FunctionCall("setLong", "stat").byItself().addArguments(Integer.toString(++index), "id")
                        )
                )
        ).addContent(EMPTY_LINE);


        final FunctionDeclaration createRecordFunction = new FunctionDeclaration("createRecord").visibility(Visibility.PRIVATE).addContent(
                new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal()).addContent(
                new VarDeclaration("long", "id", new FunctionCall("createRecord").addArgument("transaction")).markAsFinal());

        addOneToManyRelationshipDBUpdateFunctionCalls(createRecordFunction);

        createRecordFunction.addContent(
                new FunctionCall("createExtraDbActions").byItself().addArguments("transaction", "id")
        ).addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(EMPTY_LINE).addContent(
                new Assignment("this.id", "id")
        ).addContent(
                new FunctionCall("postCreateActions").byItself()
        );

        javaClass.addContent(createRecordFunction).addContent(EMPTY_LINE);

        final FunctionDeclaration createRecordFunctionWithTransaction =
                new FunctionDeclaration("createRecord", "long").addArgument(new FunctionArgument("DBTransaction", "transaction")).visibility(Visibility.PRIVATE);

        if (columns.hasItemOrder()) {
            final Column itemOrderField = columns.getItemOrderField();
            final IfBlock uninitializedItemOrderCase = new IfBlock(new Condition(new Comparison("itemOrder", "0")));
            if (itemOrderField.isUnique())
                uninitializedItemOrderCase.addContent(
                        new Assignment("itemOrder",
                                new OperatorExpression(getMaxItemOrderFunctionCall(columns.getItemOrderField(), true, false), "1", OperatorExpression.Operator.ADD))
                );
            else {
                uninitializedItemOrderCase.addContent(
                        new IfBlock(new Condition(new Comparison(uncapitalize(camelize(itemOrderField.getItemOrderAssociatedField())), "0"))).addContent(
                                new Assignment("itemOrder",
                                        new OperatorExpression(getMaxItemOrderFunctionCall(columns.getItemOrderField(), true, true), "1", OperatorExpression.Operator.ADD))
                        ).elseClause(new ElseBlock().addContent(
                                new Assignment("itemOrder",
                                        new OperatorExpression(getMaxItemOrderFunctionCall(columns.getItemOrderField(), true, false), "1", OperatorExpression.Operator.ADD))
                        ))
                );
            }

            createRecordFunctionWithTransaction.addContent(uninitializedItemOrderCase).addContent(EMPTY_LINE);
        }

        createRecordFunctionWithTransaction.addContent(
                new ReturnStatement(new FunctionCall("addRecordCreation", "transaction")
                        .addArgument(quickQuote(getInsertSQLQuery()))
                        .addArgument(new ObjectCreation("RecordCreationSetup")))
        );

        javaClass.addContent(createRecordFunctionWithTransaction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("createExtraDbActions")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addArgument(new FunctionArgument("long", "id")).visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postCreateActions").visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
    }

    private void addOneToManyRelationshipDBUpdateFunctionCalls(final FunctionDeclaration function) {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            if (!relationship.isListOnly())
                function.addContent(
                        new FunctionCall("update" + capitalize(relationship.getJavaName()) + "InDB").byItself().addArguments("transaction", "id")
                );
        }
    }

    private void addUpdate() {
        final FunctionDeclaration updateRecordFunction = new FunctionDeclaration("updateRecord").visibility(Visibility.PRIVATE).addContent(
                new VarDeclaration("DBTransaction", "transaction", new FunctionCall("createDBTransaction")).markAsFinal()
        ).addContent(
                new FunctionCall("updateRecord").byItself().addArgument("transaction")
        );

        addOneToManyRelationshipDBUpdateFunctionCalls(updateRecordFunction);

        updateRecordFunction.addContent(
                new FunctionCall("updateExtraDbActions").byItself().addArgument("transaction")
        ).addContent(
                new FunctionCall("commit", "transaction").byItself()
        ).addContent(
                new FunctionCall("postUpdateActions").byItself()
        );

        javaClass.addContent(updateRecordFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("updateRecord").addArgument(new FunctionArgument("DBTransaction", "transaction")).visibility(Visibility.PRIVATE).addContent(
                        new FunctionCall("addUpdate", "transaction").byItself()
                                .addArgument(quickQuote(getUpdateSQLQuery()))
                                .addArgument(new ObjectCreation("RecordUpdateSetup"))
                )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("updateExtraDbActions").visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("postUpdateActions").visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE);
    }

    private void addOneToManyRelationshipInDB() {
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
            if (!relationship.isListOnly()) {
                final FunctionDeclaration updateRelationshipFunction = new FunctionDeclaration("update" + capitalize(relationship.getJavaName()) + "InDB")
                        .addArgument(new FunctionArgument("DBTransaction", "transaction"))
                        .addArgument(new FunctionArgument("long", "id"))
                        .visibility(Visibility.PRIVATE);

                final FunctionCall dbAccessFunction = new FunctionCall("addUpdate", "transaction").byItself();
                updateRelationshipFunction.addContent(
                        dbAccessFunction
                                .addArgument(quickQuote(getDeleteOneToManyRelationshipQuery(relationship.getTable(), relationship.getIdSqlName())))
                                .addArgument(new AnonymousClassCreation("DBQuerySetup").setContext(dbAccessFunction).addContent(
                                        new FunctionDeclaration("setupPreparedStatement").annotate("@Override").addException("SQLException")
                                                .addArgument(new FunctionArgument("PreparedStatement", "stat")).addContent(
                                                new FunctionCall("setLong", "stat").byItself().addArguments("1", "id")
                                        )
                                ))
                ).addContent(EMPTY_LINE);

                final String var = uncapitalize(relationship.getBeanClass());
                updateRelationshipFunction.addContent(
                        new ForLoop(relationship.getBeanClass() + " " + var + ": " + relationship.getJavaName()).addContent(
                                new FunctionCall("resetId", var).byItself()
                        ).addContent(
                                new FunctionCall("setId" + beanName, var).addArgument("id").byItself()
                        ).addContent(
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
                    new FunctionDeclaration("formatDate", "String").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("Date", "date")).addContent(
                            new ReturnStatement(
                                    new FunctionCall(
                                            "format",
                                            new FunctionCall(
                                                    "getDateInstance",
                                                    "DateFormat").addArgument("DateFormat.LONG").addArgument(new FunctionCall("getLocale", internalsVar))
                                    ).addArgument("date")
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToDate", "Date").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
                            new ReturnStatement(
                                    new FunctionCall("getDateFromYYMD", "Dates").addArguments("str", quickQuote("-"))
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Date", "date")).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateDateFormat", "boolean").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
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
                    new FunctionDeclaration("formatTime", "String").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("Time", "time")).addContent(
                            new ReturnStatement(
                                    new FunctionCall(
                                            "format",
                                            new FunctionCall(
                                                    "getTimeInstance",
                                                    "DateFormat").addArgument("DateFormat.LONG").addArgument(new FunctionCall("getLocale", internalsVar))
                                    ).addArgument("time")
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToTime", "Time").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
                            new ReturnStatement(
                                    new FunctionCall("getTimeFromString", "Dates").addArguments("str", quickQuote(":"))
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Time", "time")).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateTimeFormat", "boolean").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
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
                    new FunctionDeclaration("formatTimestamp", "String").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("Timestamp", "timestamp")).addContent(
                            new ReturnStatement(
                                    new FunctionCall(
                                            "format",
                                            new FunctionCall(
                                                    "getDateTimeInstance",
                                                    "DateFormat").addArguments("DateFormat.LONG", "DateFormat.LONG").addArgument(new FunctionCall("getLocale", internalsVar))
                                    ).addArgument("timestamp")
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("convertStringToTimestamp", "Timestamp").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
                            new ReturnStatement(
                                    new FunctionCall("getTimestampFromYYMD", "Dates").addArguments("str", quickQuote("-"), quickQuote(":"))
                            )
                    )
            ).addContent(EMPTY_LINE);

            javaClass.addContent(getTemporalConvertToStringFunction("Timestamp", "timestamp")).addContent(EMPTY_LINE);

            javaClass.addContent(
                    new FunctionDeclaration("validateTimestampFormat", "boolean").visibility(Visibility.PROTECTED).addArgument(new FunctionArgument("String", "str")).addContent(
                            new VarDeclaration(
                                    "SimpleInputTimestampFormat", "simpleInputTimestampFormat",
                                    new ObjectCreation("SimpleInputTimestampFormat")
                                            .addArguments("SimpleInputDateFormat.ElementOrder.YYMD", quickQuote("-"), quickQuote(":"))
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

        final FunctionCall dbAccessFunction = new FunctionCall("processQuery", "dbAccess").byItself();
        javaClass.addContent(
                new FunctionDeclaration("getAll", "List<" + beanName + ">").markAsStatic().visibility(Visibility.PROTECTED)
                        .addArgument(new FunctionArgument("String", "orderBy")).addContent(
                        VarDeclaration.createListDeclaration(beanName, "all").markAsFinal()
                ).addContent(EMPTY_LINE).addContent(
                        dbAccessFunction
                                .addArgument(quickQuote("SELECT id FROM " + tableName + " ORDER BY ") + " + orderBy")
                                .addArgument(new AnonymousClassCreation("DBQueryProcess").setContext(dbAccessFunction).addContent(
                                        new FunctionDeclaration("processResultSet").annotate("@Override").addException("SQLException")
                                                .addArgument(new FunctionArgument("ResultSet", "rs")).addContent(
                                                new WhileBlock(new Condition("rs.next()")).addContent(
                                                        new FunctionCall("add", "all").byItself()
                                                                .addArgument(new ObjectCreation(beanName).addArgument(new FunctionCall("getLong", "rs").addArgument("1")))
                                                )
                                        )
                                ))
                ).addContent(EMPTY_LINE).addContent(
                        new ReturnStatement("all")
                )
        ).addContent(EMPTY_LINE);
    }

    private void addGetIdNamePairs() {
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
                new FunctionDeclaration("isIdOK", "boolean").markAsStatic().addArgument(new FunctionArgument("long", "id")).addContent(
                        new ReturnStatement(new FunctionCall("isIdOK", "DBQueries").addArguments("db", quickQuote(tableName), "id"))
                )
        ).addContent(EMPTY_LINE);
	}

    private void addHumanReadableTitle() {
        javaClass.addContent(
                new FunctionDeclaration("getHumanReadableTitle", "String").markAsStatic().addArgument(new FunctionArgument("long", "id")).addContent(
                        new ReturnStatement(new FunctionCall("getHumanReadableTitle", "DBQueries")
                                .addArguments("db", quickQuote(tableName), "id")
                                .addArgument(new FunctionCall("getNamingFields", parametersVar)))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addSetLocale() {
        javaClass.addContent(
                new FunctionDeclaration("setLocale").addArgument(new FunctionArgument("Locale", "locale")).addContent(
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
		addSetters();
		addGetters();
        addLabelGetters();
		addRequiredIndicators();
        addUniqueIndicators();
		addOneToManyRelationshipManagement();
        addItemOrderManagement();
		addUpdateDB();
		addDataOK();
		addReset();
		addDelete();
		addCreate();
		addUpdate();
		addOneToManyRelationshipInDB();
		addTemporalFunctions();
		addGetAll();
		addGetIdNamePairs();
		addGetCount();
		addIdOK();
		addHumanReadableTitle();
		addSetLocale();
	}
	
	private String getReadSQLQuery() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("SELECT ");
		
		for (Column column: columns.getList()) {
			final String name = column.getSqlName();
			if (!name.equals("id")) {
				buf.append(name);
				buf.append(", ");
			}
		}
		buf.delete(buf.length() - 2, buf.length());
		
		buf.append(" FROM ");
		buf.append(tableName);
		buf.append(" WHERE id=?");
		
		return buf.toString();
	}

    private String getReadSQLQueryOneToManyRelationship(final String tableName, final String indexField) {
        return "SELECT id FROM " + tableName + " WHERE " + indexField + "=?";
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
				buf.append(name);
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
				buf.append(name);
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
}

