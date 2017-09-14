package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.ForLoop;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.VarDeclaration;

public class LabelsSourceFile extends BaseCode {

    public LabelsSourceFile(final String packageName) {
        super("Labels", packageName);

        createSourceCode();
    }

    private void addImports() {
        importsManager.addImport("org.beanmaker.util.DbBeanLabel");
        importsManager.addImport("org.beanmaker.util.DbBeanLanguage");
        importsManager.addImport("org.beanmaker.util.MissingImplementationException");

        importsManager.addImport("java.util.Arrays");
        importsManager.addImport("java.util.HashMap");
        importsManager.addImport("java.util.List");
        importsManager.addImport("java.util.Map");
    }

    private void addNonImplementedFunctions() {
        addNonImplementedFunction("DbBeanLabel", "get", new FunctionArgument("long", "id"));

        addNonImplementedFunction("boolean", "isIdOK", new FunctionArgument("long", "id"));

        addNonImplementedFunction(
                "String",
                "get",
                new FunctionArgument("long", "id"),
                new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"));

        addNonImplementedFunction(
                "String",
                "get",
                new FunctionArgument("String", "name"),
                new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"));

        addNonImplementedFunction("DbBeanLabel", "createInstance");

        addNonImplementedFunction("List<DbBeanLanguage>", "getAllActiveLanguages");

        addNonImplementedFunction(
                "DbBeanLanguage",
                "getLanguage",
                new FunctionArgument("long", "id"));
    }

    private void addNonImplementedFunction(
            final String returnType,
            final String name,
            final FunctionArgument... functionArguments)
    {
            final FunctionDeclaration functionDeclaration =
                    new FunctionDeclaration(name, returnType)
                            .markAsStatic();

            final StringBuilder argTypeList = new StringBuilder();
            for (FunctionArgument argument: functionArguments) {
                functionDeclaration.addArgument(argument);
                argTypeList.append(argument.getType()).append(", ");
            }
            if (argTypeList.length() > 0)
                argTypeList.delete(argTypeList.length() - 2, argTypeList.length());

            functionDeclaration.addContent(
                    new ExceptionThrow("MissingImplementationException")
                            .addArgument(Strings.quickQuote("Labels." + name + "(" + argTypeList.toString() + ")"))
            );

            javaClass.addContent(functionDeclaration).addContent(EMPTY_LINE);
    }

    private void addLanguageCopyFunction() {
        javaClass.addContent(
                new FunctionDeclaration("getCopy", "DbBeanLanguage")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getLanguage")
                                                .addArgument(new FunctionCall("getId", "dbBeanLanguage"))
                                )
                        )
        ).addContent(EMPTY_LINE);
    }

    private void addLabelMapFunctions() {
        javaClass.addContent(
                new FunctionDeclaration("getLabelMap", "Map<String, String>")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addArgument(new FunctionArgument("String...", "labelNames"))
                        .addContent(
                                new ReturnStatement(
                                        new FunctionCall("getLabelMap")
                                                .addArgument("dbBeanLanguage")
                                                .addArgument(new FunctionCall("asList", "Arrays")
                                                        .addArgument("labelNames"))
                                )
                        )
        ).addContent(EMPTY_LINE);

        javaClass.addContent(
                new FunctionDeclaration("getLabelMap", "Map<String, String>")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"))
                        .addArgument(new FunctionArgument("List<String>", "labelNames"))
                        .addContent(
                                VarDeclaration.createGenericContainerDeclaration(
                                        "Map",
                                        "HashMap",
                                        "String, String",
                                        "labelMap")
                                        .markAsFinal()
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ForLoop("String labelName: labelNames")
                                        .addContent(
                                                new FunctionCall("put", "labelMap")
                                                        .byItself()
                                                        .addArgument("labelName")
                                                        .addArgument(
                                                                new FunctionCall("get")
                                                                        .addArguments("labelName", "dbBeanLanguage")
                                                        )
                                        )
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement("labelMap")
                        )
        );
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addImports();
        addNonImplementedFunctions();
        addLanguageCopyFunction();
        addLabelMapFunctions();
    }
}
