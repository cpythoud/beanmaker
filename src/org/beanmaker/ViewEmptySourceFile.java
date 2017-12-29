package org.beanmaker;

public class ViewEmptySourceFile extends BaseCode {

    public ViewEmptySourceFile(final String beanName, final String packageName, final String viewName) {
        super(beanName + viewName + "View", packageName);
        javaClass.extendsClass(beanName + viewName + "ViewBase");
        javaClass.markAsFinal();
        createSourceCode();
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        newLine();
    }
}
