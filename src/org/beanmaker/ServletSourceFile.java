package org.beanmaker;

public class ServletSourceFile extends EmptySourceFile {

    public ServletSourceFile(String beanName, String packageName) {
        super(beanName + "Servlet", packageName, beanName + "ServletBase");
    }
}
