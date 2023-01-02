package org.beanmaker;

import org.jcodegen.java.ClassSourceFile;
import org.jcodegen.java.Configuration;
import org.jcodegen.java.EmptyLine;
import org.jcodegen.java.ExceptionThrow;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ImportsManager;
import org.jcodegen.java.JavaClass;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import org.dbbeans.util.Strings;

public abstract class BaseCode {
    protected final ClassSourceFile sourceFile;
    protected final JavaClass javaClass;
    protected final ImportsManager importsManager;

    protected final String className;

    protected static final EmptyLine EMPTY_LINE = new EmptyLine();
    protected static final String EMPTY_STRING = "\"\"";


    public BaseCode(final String className, final String packageName) {
        Configuration.setCurrentConfiguration(
                Configuration.builder().setDefaultDeclarationVisibility(Visibility.PUBLIC).create()
        );

        if (Strings.isEmpty(className))
            throw new IllegalArgumentException("className empty");

        if (Strings.isEmpty(packageName))
            throw new IllegalArgumentException("packageName empty");

        sourceFile = new ClassSourceFile(packageName, className);
        javaClass = sourceFile.getJavaClass();
        importsManager = sourceFile.getImportsManager();

        this.className = className;
    }

    public String getSourceCode() {
        return sourceFile.toString();
    }

    public String getFilename() {
        return sourceFile.getFilename();
    }


    protected void newLine() {
        javaClass.addContent(EMPTY_LINE);
    }


    protected void addProperty(final VarDeclaration property) {
        javaClass.addContent(property.visibility(Visibility.PRIVATE));
    }

    protected void addProperty(final String type, final String var) {
        addProperty(new VarDeclaration(type, var));
    }

    protected void addProperty(final String type, final String var, final String val) {
        addProperty(new VarDeclaration(type, var, val));
    }

    protected void addInheritableProperty(final VarDeclaration property) {
        javaClass.addContent(property.visibility(Visibility.PROTECTED));
    }

    protected void addInheritableProperty(final String type, final String var) {
        addInheritableProperty(new VarDeclaration(type, var));
    }

    protected void addInheritableProperty(final String type, final String var, final String val) {
        addInheritableProperty(new VarDeclaration(type, var, val));
    }

    protected void addInheritableProperty(final String type, final String var, final ObjectCreation object) {
        addInheritableProperty(new VarDeclaration(type, var, object));
    }

    protected String getVarNameForClass(final String className) {
        final String[] parts = className.split("\\.");
        return Strings.uncapitalize(parts[parts.length - 1]);
    }

    protected void addNonImplementedFunction(final String name, final FunctionArgument... functionArguments) {
        addNonImplementedFunction(null, name, false, false, Visibility.PUBLIC, functionArguments);
    }

    protected void addNonImplementedOverriddenFunction(
            final String returnType,
            final String name,
            final Visibility visibility,
            final FunctionArgument... functionArguments)
    {
        addNonImplementedFunction(returnType, name, false, true, visibility, functionArguments);
    }

    protected void addNonImplementedStaticFunction(
            final String returnType,
            final String name,
            final FunctionArgument... functionArguments)
    {
        addNonImplementedFunction(returnType, name, true, false, Visibility.PUBLIC, functionArguments);
    }

    private void addNonImplementedFunction(
            final String returnType,
            final String name,
            final boolean staticFunction,
            final boolean overridden,
            final Visibility visibility,
            final FunctionArgument... functionArguments)
    {
        final FunctionDeclaration functionDeclaration;
        if (returnType == null)
            functionDeclaration = new FunctionDeclaration(name);
        else
            functionDeclaration = new FunctionDeclaration(name, returnType);

        functionDeclaration.visibility(visibility);

        if (staticFunction)
            functionDeclaration.markAsStatic();

        if (overridden)
            functionDeclaration.annotate("@Override");

        final StringBuilder argTypeList = new StringBuilder();
        for (FunctionArgument argument: functionArguments) {
            functionDeclaration.addArgument(argument);
            argTypeList.append(argument.getType()).append(", ");
        }
        if (argTypeList.length() > 0)
            argTypeList.delete(argTypeList.length() - 2, argTypeList.length());

        functionDeclaration.addContent(
                new ExceptionThrow("MissingImplementationException")
                        .addArgument(Strings.quickQuote(
                                className + "." + name + "(" + argTypeList.toString() + ")"))
        );

        javaClass.addContent(functionDeclaration).addContent(EMPTY_LINE);
    }

}
