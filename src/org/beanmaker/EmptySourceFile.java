package org.beanmaker;

public class EmptySourceFile extends BaseCode {

    public EmptySourceFile(final String className, final String packageName) {
        this(className, packageName, null);
    }

    public EmptySourceFile(final String className, final String packageName, final String parentClassName) {
        super(className, packageName);
        if (parentClassName != null)
            javaClass.extendsClass(parentClassName);

        createSourceCode();
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        newLine();
    }
}
