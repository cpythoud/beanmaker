package org.beanmaker.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.dbbeans.util.Money;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.util.Map;

public abstract class BaseExcelExport extends TabularView {

    protected String sheetName;

    protected ExcelFormats excelFormats;

    protected boolean hasSuperTitleRow = false;

    public BaseExcelExport(String resourceBundleName, String sheetName) {
        super(resourceBundleName);
        this.sheetName = sheetName;
    }

    protected void setLanguage(DbBeanLanguage dbBeanLanguage, final Map<String, String> labels) {
        this.dbBeanLanguage = dbBeanLanguage;

        yesName = labels.get("yes");
        noName = labels.get("no");

        setLocale(dbBeanLanguage.getLocale());
    }

    public Workbook getExcelWorkbook() {
        return getExcelWorkbook(sheetName);
    }

    public Workbook getExcelWorkbook(final String sheetName) {
        final Workbook excelWorkbook = new XSSFWorkbook();
        excelFormats = new DefaultExcelFormats(excelWorkbook, yesName, noName);

        final Sheet sheet = excelWorkbook.createSheet(sheetName);
        addSuperTitleRowTo(sheet);
        addTitleRowTo(sheet);
        addDataRowsTo(sheet);

        return excelWorkbook;
    }

    public String getFilename() {
        return sheetName + ".xlsx";
    }

    protected void addSuperTitleRowTo(final Sheet sheet) { }

    protected int addAdHocSuperTitleCell(
            final Sheet sheet,
            final Row superTitleRow,
            final String text,
            final int startingCellNumber,
            final int cellCount)
    {
        return excelFormats.addSuperTitleCell(sheet, superTitleRow, text, startingCellNumber, cellCount);
    }

    protected int addSuperTitleCell(
            final Sheet sheet,
            final Row superTitleRow,
            final String propertyName,
            final int startingCellNumber,
            final int cellCount)
    {
        if (propertyName == null)
            throw new NullPointerException("propertyName cannot be null");

        return addAdHocSuperTitleCell(
                sheet,
                superTitleRow,
                resourceBundle.getString(propertyName),
                startingCellNumber,
                cellCount);
    }

    protected abstract void addTitleRowTo(final Sheet sheet);

    protected void addAdHocTitleCell(final Row titleRow, final String text, final int cellNumber) {
       excelFormats.addTitleCell(titleRow, text, cellNumber);
    }

    protected void addTitleCell(final Row titleRow, final String resourceLabel, final int cellNumber) {
        addAdHocTitleCell(titleRow, resourceBundle.getString(resourceLabel), cellNumber);
    }

    protected void addIdTitleCell(final Row titleRow, final int cellNumber) {
        addTitleCell(titleRow, "id", cellNumber);
    }

    protected abstract void addDataRowsTo(final Sheet sheet);

    protected void addIdCell(final Row row, final DbBeanInterface bean, final int cellNumber) {
        excelFormats.addIdCell(row, bean, cellNumber);
    }

    protected void addDataCell(final Row row, final int cellNumber, final String data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final boolean data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final int data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final long data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Date data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Time data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Timestamp data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Money data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final double data) {
        excelFormats.addDataCell(row, cellNumber, data);
    }

    protected void autosizeColumns(final Sheet sheet, final int columns) {
        excelFormats.autosizeColumns(sheet, columns);
    }
}
