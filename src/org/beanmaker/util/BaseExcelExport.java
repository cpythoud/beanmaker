package org.beanmaker.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbbeans.util.Money;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

public abstract class BaseExcelExport extends TabularView {

    protected String sheetName;

    protected CellStyle superHeaderFormat;
    protected CellStyle headerFormat;
    protected CellStyle dateFormat;
    protected CellStyle timeFormat;
    protected CellStyle timestampFormat;
    protected CellStyle integerFormat;
    protected CellStyle decimalFormat;

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
        initDateFormat(excelWorkbook);
        initTimeFormat(excelWorkbook);
        initTimestampFormat(excelWorkbook);
        initSuperHeaderFormat(excelWorkbook);
        initHeaderFormat(excelWorkbook);
        initIntegerFormat(excelWorkbook);
        initDecimalFormat(excelWorkbook);

        final Sheet sheet = excelWorkbook.createSheet(sheetName);
        addSuperTitleRowTo(sheet);
        addTitleRowTo(sheet);
        addDataRowsTo(sheet);

        return excelWorkbook;
    }

    public String getFilename() {
        return sheetName + ".xlsx";
    }

    protected void initSuperHeaderFormat(final Workbook excelWorkbook) {
        final Font font = excelWorkbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        superHeaderFormat = excelWorkbook.createCellStyle();
        superHeaderFormat.setFont(font);
    }

    protected void initHeaderFormat(final Workbook excelWorkbook) {
        final Font font = excelWorkbook.createFont();
        font.setBold(true);

        headerFormat = excelWorkbook.createCellStyle();
        headerFormat.setFont(font);
    }

    protected void initDateFormat(final Workbook excelWorkbook) {
        dateFormat = excelWorkbook.createCellStyle();
        dateFormat.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("m/d/yy"));
    }

    protected void initTimeFormat(final Workbook excelWorkbook) {
        timeFormat = excelWorkbook.createCellStyle();
        timeFormat.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("h:mm:ss"));
    }

    protected void initTimestampFormat(final Workbook excelWorkbook) {
        timestampFormat = excelWorkbook.createCellStyle();
        timestampFormat.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("m/d/yy h:mm"));
    }

    protected void initIntegerFormat(final Workbook excelWorkbook) {
        integerFormat = excelWorkbook.createCellStyle();
        integerFormat.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("0"));
    }

    protected void initDecimalFormat(final Workbook excelWorkbook) {
        decimalFormat = excelWorkbook.createCellStyle();
        decimalFormat.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("0.00"));
    }

    protected String getYesOrNoRepresentation(final boolean value) {
        if (value)
            return yesName;

        return noName;
    }

    protected void addSuperTitleRowTo(final Sheet sheet) { }

    protected int addSuperTitleCell(
            final Sheet sheet,
            final Row superTitleRow,
            final String propertyName,
            final int startingCellNumber,
            final int cellCount)
    {
        if (propertyName == null)
            throw new NullPointerException("propertyName cannot be null");
        if (startingCellNumber < 0)
            throw new IllegalArgumentException("starting cell must be 0 or positive");
        if (cellCount <= 0)
            throw new IllegalArgumentException("cell count must be 1 or more");

        superTitleRow.createCell(startingCellNumber).setCellValue(resourceBundle.getString(propertyName));
        superTitleRow.getCell(startingCellNumber).setCellStyle(superHeaderFormat);
        int nextCell = startingCellNumber + cellCount;
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startingCellNumber, nextCell - 1));

        return nextCell;
    }

    protected abstract void addTitleRowTo(final Sheet sheet);

    protected void addAdHocTitleCell(final Row titleRow, final String text, final int cellNumber) {
        titleRow.createCell(cellNumber).setCellValue(text);
        titleRow.getCell(cellNumber).setCellStyle(headerFormat);
    }

    protected void addTitleCell(final Row titleRow, final String resourceLabel, final int cellNumber) {
        addAdHocTitleCell(titleRow, resourceBundle.getString(resourceLabel), cellNumber);
    }

    protected void addIdTitleCell(final Row titleRow, final int cellNumber) {
        addTitleCell(titleRow, "id", cellNumber);
    }

    protected abstract void addDataRowsTo(final Sheet sheet);

    protected void addIdCell(final Row row, final DbBeanInterface bean, final int cellNumber) {
        row.createCell(cellNumber).setCellValue(bean.getId());
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final String data) {
        row.createCell(cellNumber).setCellValue(data);
    }

    protected void addDataCell(final Row row, final int cellNumber, final boolean data) {
        row.createCell(cellNumber).setCellValue(getYesOrNoRepresentation(data));
    }

    protected void addDataCell(final Row row, final int cellNumber, final int data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final long data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Date data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(dateFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Time data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(timeFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Timestamp data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(timestampFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final Money data) {
        row.createCell(cellNumber).setCellValue(data.getDoubleVal());
        row.getCell(cellNumber).setCellStyle(decimalFormat);
    }

    protected void addDataCell(final Row row, final int cellNumber, final double data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(decimalFormat);
    }

    protected void autosizeColumns(final Sheet sheet, final int columns) {
        for (int i = 0; i < columns; ++i)
            sheet.autoSizeColumn(i);
    }
}
