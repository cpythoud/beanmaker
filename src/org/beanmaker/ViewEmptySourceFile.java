package org.beanmaker;

public class ViewEmptySourceFile extends EmptySourceFile {

    public ViewEmptySourceFile(final String beanName, final String packageName, final String viewName) {
        super(beanName + viewName + "View", packageName, beanName + viewName + "ViewBase");
    }
}
