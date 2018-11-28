package org.beanmaker.util;

import org.apache.poi.ss.usermodel.Workbook;

public interface DbBeanExcelExport {

    void setLanguage(DbBeanLanguage dbBeanLanguage);

    Workbook getExcelWorkbook();

    Workbook getExcelWorkbook(final String sheetName);

    String getFilename();
}
