package org.beanmaker;

public class ExcelExportSourceFile extends EmptySourceFile {

    public ExcelExportSourceFile(final String beanName, final String packageName) {
        super(beanName + "ExcelExport", packageName, beanName + "ExcelExportBase");
    }
}
