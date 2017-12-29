package org.beanmaker;

import org.jcodegen.java.EmptyLine;
import org.jcodegen.java.ImportsManager;
import org.jcodegen.java.JavaClass;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.SourceFile;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import org.dbbeans.util.Strings;


public abstract class BaseCode {
    protected final SourceFile sourceFile;
    protected final JavaClass javaClass;
    protected final ImportsManager importsManager;

    protected static final EmptyLine EMPTY_LINE = new EmptyLine();
    protected static final String EMPTY_STRING = "\"\"";


    public BaseCode(final String className, final String packageName) {
        if (Strings.isEmpty(className))
            throw new IllegalArgumentException("className empty");

        if (Strings.isEmpty(packageName))
            throw new IllegalArgumentException("packageName empty");

        sourceFile = new SourceFile(packageName, className);
        javaClass = sourceFile.getJavaClass();
        importsManager = sourceFile.getImportsManager();

        javaClass.visibility(Visibility.PACKAGE_PRIVATE);
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
}
