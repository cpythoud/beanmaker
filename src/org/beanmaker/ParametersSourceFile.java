package org.beanmaker;

public class ParametersSourceFile extends BaseCode {

    public ParametersSourceFile(final String beanName, final String packageName) {
        super(beanName + "Parameters", packageName);

        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());
        javaClass.extendsClass(beanName + "ParametersBase");
    }
}
