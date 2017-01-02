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
import org.jcodegen.java.OperatorExpression;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import static org.beanmaker.SourceFiles.chopId;

import org.dbbeans.util.Strings;
import static org.dbbeans.util.Strings.capitalize;
import static org.dbbeans.util.Strings.quickQuote;
import static org.dbbeans.util.Strings.uncapitalize;

public class BaseXMLViewSourceFile extends ViewCode {

    public BaseXMLViewSourceFile(final String beanName, final String packageName, final Columns columns, final String tableName) {
        super(beanName, packageName, "XMLViewBase", columns, tableName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.jcodegen.html.xmlbase.ValueXMLAttribute");
        importsManager.addImport("org.jcodegen.html.xmlbase.XMLElement");

        importsManager.addImport("org.beanmaker.util.DbBeanViewInterface");
    }

    private void addXMLGetter() {
        javaClass.addContent(
                new FunctionDeclaration("getXML", "String").addContent(
                        new ReturnStatement(new OperatorExpression(
                                new FunctionCall("getXMLPrefix"),
                                new FunctionCall("toString", new FunctionCall("getXMLElement").addArgument("true")),
                                OperatorExpression.Operator.ADD
                        ))
                )
        ).addContent(EMPTY_LINE);

		javaClass.addContent(
                new FunctionDeclaration("getXMLPrefix", "String").visibility(Visibility.PROTECTED).addContent(
                        new ReturnStatement("\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?>\\n\"")
                )
        ).addContent(EMPTY_LINE);

		final FunctionDeclaration getXMLElementFunction = new FunctionDeclaration("getXMLElement", "XMLElement")
                .addArgument(new FunctionArgument("boolean", "recursion"));

        getXMLElementFunction.addContent(
                ifNotDataOK(true).addContent(
                        ExceptionThrow.getThrowExpression("IllegalArgumentException", "Cannot compute xml from bad data")
                )
        ).addContent(EMPTY_LINE).addContent(
                new VarDeclaration("XMLElement", "root", new ObjectCreation("XMLElement").addArgument(quickQuote(beanVarName))).markAsFinal()
        ).addContent(
                new FunctionCall("addAttribute", "root").byItself()
                        .addArgument(new ObjectCreation("ValueXMLAttribute")
                                .addArgument(quickQuote("id"))
                                .addArgument(new FunctionCall("toString", "Long").addArgument(getId())))
        ).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String field = column.getJavaName();
                if (field.startsWith("id") && column.hasAssociatedBean()) {
                    getXMLElementFunction.addContent(
                            new IfBlock(new Condition("recursion")
                                    .andCondition(new Condition(new FunctionCall("is" + capitalize(field) + "Empty", beanVarName), true).needsParentheses()
                                            .orCondition(new Condition(new Comparison(getFieldValue(field), "0"))))).addContent(
                                    new FunctionCall("addChild", "root").byItself()
                                            .addArgument(new FunctionCall("get" + chopId(field) + "XMLElement"))
                            )
                    );
                } else {
                    final FunctionCall addChild = new FunctionCall("addChild", "root").byItself()
                            .addArgument(new FunctionCall("get" + capitalize(column.getJavaName()) + "XMLElement"));
                    if (!column.getJavaType().equals("boolean"))
                        getXMLElementFunction.addContent(
                                new IfBlock(new Condition(new FunctionCall("is" + capitalize(field) + "Empty", beanVarName), true)).addContent(addChild)
                        );
                    else
                        getXMLElementFunction.addContent(addChild);
                }
            }
        }

        if (columns.hasOneToManyRelationships()) {
            for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
                if (!relationship.isListOnly())
                    getXMLElementFunction.addContent(
                            new IfBlock(new Condition(new Comparison(new FunctionCall("size", getFieldValue(relationship.getJavaName())), "0", Comparison.Comparator.GREATER_THAN))).addContent(
                                    new FunctionCall("addChildren", "root").byItself().addArgument(new FunctionCall("get" + capitalize(relationship.getJavaName()) + "XMLElements"))
                            )
                    );
            }
        }

        getXMLElementFunction.addContent(EMPTY_LINE).addContent(
                new ReturnStatement("root")
        );
        javaClass.addContent(getXMLElementFunction).addContent(EMPTY_LINE);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final String type = column.getJavaType();
                final String field = column.getJavaName();
                final FunctionDeclaration getXMLElement;
                if (field.startsWith("id") && column.hasAssociatedBean()) {
                    final String associatedBeanClass = column.getAssociatedBeanClass();
                    getXMLElement = new FunctionDeclaration("get" + chopId(field) + "XMLElement", "XMLElement").addContent(
                            new VarDeclaration(associatedBeanClass + "XMLView", uncapitalize(associatedBeanClass)  + "XMLView",
                                    new ObjectCreation(associatedBeanClass + "XMLView").addArgument(getFieldValue(chopId(field)))).markAsFinal()
                    ).addContent(EMPTY_LINE).addContent(
                            new ReturnStatement(new FunctionCall("getXMLElement", uncapitalize(associatedBeanClass) + "XMLView").addArgument("false"))
                    );
                } else {
                    getXMLElement = new FunctionDeclaration("get" + capitalize(field) + "XMLElement", "XMLElement");
                    final ObjectCreation xmlElementCreation = new ObjectCreation("XMLElement");
                    xmlElementCreation.addArgument(quickQuote(field));
                    if (type.equals("boolean"))
                        xmlElementCreation.addArgument(new FunctionCall("toString", "Boolean").addArgument(new FunctionCall("is" + Strings.capitalize(field), beanVarName)));
                    else if (type.equals("int"))
                        xmlElementCreation.addArgument(getFieldValue(field + "Str"));
                    else if (type.equals("long"))
                        xmlElementCreation.addArgument(getFieldValue(field + "Str"));
                    else if (JAVA_TEMPORAL_TYPES.contains(type) || type.equals("Money"))
                        xmlElementCreation.addArgument(getFieldValue(field + "Str"));
                    else
                        xmlElementCreation.addArgument(getFieldValue(field));
                    getXMLElement.addContent(
                            new ReturnStatement(xmlElementCreation)
                    );
                }
                javaClass.addContent(getXMLElement.visibility(Visibility.PROTECTED)).addContent(EMPTY_LINE);
            }
        }

        if (columns.hasOneToManyRelationships()) {
            for (OneToManyRelationship relationship: columns.getOneToManyRelationships()) {
                if (!relationship.isListOnly()) {
                    importsManager.addImport("java.util.List");
                    importsManager.addImport("java.util.ArrayList");
                    javaClass.addContent(
                            new FunctionDeclaration("get" + capitalize(relationship.getJavaName()) + "XMLElements", "List<XMLElement>")
                                    .visibility(Visibility.PROTECTED).addContent(
                                    VarDeclaration.createListDeclaration("XMLElement", "xmlElements").markAsFinal()
                            ).addContent(EMPTY_LINE).addContent(
                                    new ForEach(relationship.getBeanClass(), uncapitalize(relationship.getBeanClass()), getFieldValue(relationship.getJavaName())).addContent(
                                            new VarDeclaration(relationship.getBeanClass() + "XMLView", uncapitalize(relationship.getBeanClass()) + "XMLView",
                                                    new ObjectCreation(relationship.getBeanClass() + "XMLView").addArgument(uncapitalize(relationship.getBeanClass()))).markAsFinal()
                                    ).addContent(
                                            new FunctionCall("add", "xmlElements").byItself()
                                                    .addArgument(new FunctionCall("getXMLElement", uncapitalize(relationship.getBeanClass()) + "XMLView").addArgument("false"))
                                    )
                            ).addContent(EMPTY_LINE).addContent(
                                    new ReturnStatement("xmlElements")
                            )
                    ).addContent(EMPTY_LINE);
                }
            }
        }
	}

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        javaClass.markAsAbstract().implementsInterface("DbBeanViewInterface");
        addViewPrelude();
        addXMLGetter();
    }
}
