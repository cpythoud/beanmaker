package org.beanmaker;

import org.jcodegen.java.Comparison;
import org.jcodegen.java.Condition;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.ForEach;
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

public class BaseJsonViewSourceFile extends ViewCode {

    public BaseJsonViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "JsonViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.dbbeans.util.json.JsonObject");
        importsManager.addImport("org.dbbeans.util.json.JsonElement");
        importsManager.addImport("org.dbbeans.util.json.JsonIntegerElement");
    }

    private void addJsonGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getJSON", "String").addContent(
                        new ReturnStatement(new FunctionCall("toString", new FunctionCall("getJsonObject").addArgument("true")))
                )
        ).addContent(EMPTY_LINE);

        final FunctionDeclaration getJsonObjectFunction = new FunctionDeclaration("getJsonObject", "JsonObject")
                .addArgument(new FunctionArgument("boolean", "recursion")).addContent(
                        ifNotDataOK(true).addContent(
                                ExceptionThrow.getThrowExpression("IllegalArgumentException", "Cannot compute json from bad data")
                        )
                ).addContent(EMPTY_LINE).addContent(
                        VarDeclaration.declareAndInitFinal("JsonObject", "json")
                ).addContent(EMPTY_LINE).addContent(
                        new FunctionCall("addElement", "json").byItself().addArgument(new FunctionCall("getIdJsonElement"))
                );

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();
                if (field.startsWith("id") && column.hasAssociatedBean()) {
                    getJsonObjectFunction.addContent(
                            new IfBlock(new Condition("recursion")
                                    .andCondition(new Condition(new FunctionCall("is" + capitalize(field) + "Empty", beanVarName), true).needsParentheses()
                                            .orCondition(new Condition(new Comparison(getFieldValue(field), "0"))))).addContent(
                                    new FunctionCall("addElement", "json").byItself()
                                            .addArgument(new FunctionCall("get" + chopId(field) + "JsonElement"))
                            )
                    );
                } else {
                    final FunctionCall addChild = new FunctionCall("addElement", "json").byItself()
                            .addArgument(new FunctionCall("get" + capitalize(column.getJavaName()) + "JsonElement"));
                    if (!column.getJavaType().equals("boolean"))
                        getJsonObjectFunction.addContent(
                                new IfBlock(new Condition(new FunctionCall("is" + capitalize(field) + "Empty", beanVarName), true)).addContent(addChild)
                        );
                    else
                        getJsonObjectFunction.addContent(addChild);
                }
            }
        }

        if (columns.hasOneToManyRelationships()) {
            for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
                if (!relationship.isListOnly())
                    getJsonObjectFunction.addContent(
                            new IfBlock(new Condition(new Comparison(new FunctionCall("size", getFieldValue(relationship.getJavaName())), "0", Comparison.Comparator.GREATER_THAN))).addContent(
                                    new FunctionCall("addElement", "json").byItself().addArgument(new FunctionCall("get" + capitalize(relationship.getJavaName()) + "JsonElement"))
                            )
                    );
            }
        }

        getJsonObjectFunction.addContent(EMPTY_LINE).addContent(
                new ReturnStatement("json")
        );

        javaClass.addContent(getJsonObjectFunction).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getIdJsonElement", "JsonElement").visibility(Visibility.PROTECTED).addContent(
                        new ReturnStatement(new ObjectCreation("JsonIntegerElement").addArgument(quickQuote("id")).addArgument(getId()))
                )
        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final FunctionDeclaration getJsonElementFunction;

                if (field.startsWith("id") && column.hasAssociatedBean()) {
                    importsManager.addImport("org.dbbeans.util.json.JsonObjectElement");
                    final String associatedBeanClass = column.getAssociatedBeanClass();
                    getJsonElementFunction = new FunctionDeclaration("get" + chopId(field) + "JsonElement", "JsonElement").visibility(Visibility.PROTECTED).addContent(
                            new VarDeclaration(associatedBeanClass + "JsonView", uncapitalize(associatedBeanClass) + "JsonView",
                                    new ObjectCreation(associatedBeanClass + "JsonView").addArgument(getFieldValue(chopId(field)))).markAsFinal()
                    ).addContent(EMPTY_LINE).addContent(
                            new ReturnStatement(new ObjectCreation("JsonObjectElement")
                                    .addArgument(quickQuote(uncapitalize(chopId(field))))
                                    .addArgument(new FunctionCall("getJsonObject", uncapitalize(associatedBeanClass) + "JsonView").addArgument("false")))
                    );
                } else {
                    final ObjectCreation jsonElementCreation;
                    if (type.equals("int") || type.equals("long"))
                        jsonElementCreation = new ObjectCreation("JsonIntegerElement");
                    else if (type.equals("Timestamp")) {
                        importsManager.addImport("org.dbbeans.util.json.JsonDateTimeElement");
                        jsonElementCreation = new ObjectCreation("JsonDateTimeElement");
                    } else if (type.equals("Money")) {
                        importsManager.addImport("org.dbbeans.util.json.JsonStringElement");
                        jsonElementCreation = new ObjectCreation("JsonStringElement");
                    } else {
                        final String jsonElementClass = "Json" + capitalize(type) + "Element";
                        importsManager.addImport("org.dbbeans.util.json." + jsonElementClass);
                        jsonElementCreation = new ObjectCreation(jsonElementClass);
                    }
                    jsonElementCreation.addArgument(quickQuote(field));
                    if (type.equals("Money"))
                        jsonElementCreation.addArgument(new FunctionCall("toString", getFieldValue(field)));
                    else if (type.equals("boolean"))
                        jsonElementCreation.addArgument(new FunctionCall("is" + capitalize(field), beanVarName));
                    else if(type.equals("int") || type.equals("long"))
                        jsonElementCreation.addArgument(getFieldValue(field + "Str"));
                    else
                        jsonElementCreation.addArgument(getFieldValue(field));
                    getJsonElementFunction = new FunctionDeclaration("get" + capitalize(field) + "JsonElement", "JsonElement").visibility(Visibility.PROTECTED).addContent(
                            new ReturnStatement(jsonElementCreation)
                    );
                }

                javaClass.addContent(getJsonElementFunction).addContent(EMPTY_LINE);
            }
        }

        if (columns.hasOneToManyRelationships()) {
            for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
                if (!relationship.isListOnly()) {
                    importsManager.addImport("java.util.List");
                    importsManager.addImport("java.util.ArrayList");
                    importsManager.addImport("org.dbbeans.util.json.JsonArrayOfObjectsElement");
                    javaClass.addContent(
                            new FunctionDeclaration("get" + capitalize(relationship.getJavaName()) + "JsonElement", "JsonElement").visibility(Visibility.PROTECTED).addContent(
                                    VarDeclaration.createListDeclaration("JsonObject", "jsonObjects").markAsFinal()
                            ).addContent(EMPTY_LINE).addContent(
                                    new ForEach(relationship.getBeanClass(), uncapitalize(relationship.getBeanClass()), getFieldValue(relationship.getJavaName())).addContent(
                                            new VarDeclaration(relationship.getBeanClass() + "JsonView", uncapitalize(relationship.getBeanClass()) + "JsonView",
                                                    new ObjectCreation(relationship.getBeanClass() + "JsonView").addArgument(uncapitalize(relationship.getBeanClass()))).markAsFinal()
                                    ).addContent(
                                            new FunctionCall("add", "jsonObjects").byItself()
                                                    .addArgument(new FunctionCall("getJsonObject", uncapitalize(relationship.getBeanClass() + "JsonView"))
                                                            .addArgument("false"))
                                    )
                            ).addContent(EMPTY_LINE).addContent(
                                    new ReturnStatement(new ObjectCreation("JsonArrayOfObjectsElement").addArguments(quickQuote(relationship.getJavaName()), "jsonObjects"))
                            )
                    ).addContent(EMPTY_LINE);
                }
            }
        }
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        javaClass.markAsAbstract();
        addViewPrelude();
        addJsonGetter();
    }
}
