package org.beanmaker;

import org.jcodegen.java.Condition;
import org.jcodegen.java.ForEach;
import org.jcodegen.java.ForLoop;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.IfBlock;
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

        importsManager.addImport("org.dbbeans.sql.DBTransaction");
    }

    private void addNonImplementedFunctions() {
        addNonImplementedStaticFunction("DbBeanLabel", "get", new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction("boolean", "isIdOK", new FunctionArgument("long", "id"));

        addNonImplementedStaticFunction(
                "boolean",
                "isIdOK",
                new FunctionArgument("long", "id"),
                new FunctionArgument("DBTransaction", "transaction"));

        addNonImplementedStaticFunction(
                "String",
                "get",
                new FunctionArgument("long", "id"),
                new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"));

        addNonImplementedStaticFunction(
                "String",
                "get",
                new FunctionArgument("String", "name"),
                new FunctionArgument("DbBeanLanguage", "dbBeanLanguage"));

        addNonImplementedStaticFunction("DbBeanLabel", "createInstance");

        addNonImplementedStaticFunction("List<DbBeanLanguage>", "getAllActiveLanguages");

        addNonImplementedStaticFunction(
                "DbBeanLanguage",
                "getLanguage",
                new FunctionArgument("long", "id"));
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

    private void addReplaceDataFunction() {
        javaClass.addContent(
                new FunctionDeclaration("replaceData", "DbBeanLabel")
                        .markAsStatic()
                        .addArgument(new FunctionArgument("DbBeanLabel", "into"))
                        .addArgument(new FunctionArgument("DbBeanLabel", "from"))
                        .addContent(
                                new FunctionCall("clearCache", "into").byItself()
                        )
                        .addContent(
                                new ForEach("DbBeanLanguage", "dbBeanLanguage", new FunctionCall("getAllActiveLanguages"))
                                        .addContent(
                                                new IfBlock(new Condition(new FunctionCall("hasDataFor", "from").addArgument("dbBeanLanguage")))
                                                        .addContent(
                                                                new FunctionCall("updateLater", "into")
                                                                        .byItself()
                                                                        .addArgument("dbBeanLanguage")
                                                                        .addArgument(new FunctionCall("get", "from").addArgument("dbBeanLanguage"))
                                                        )
                                        )
                        )
                        .addContent(EMPTY_LINE)
                        .addContent(
                                new ReturnStatement("into")
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
        addReplaceDataFunction();
        addLabelMapFunctions();
    }
}
