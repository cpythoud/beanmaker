package org.beanmaker.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import org.dbbeans.util.Money;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class DefaultExcelFormats implements ExcelFormats {

    private final CellStyle superHeaderFormat;
    private final CellStyle headerFormat;
    private final CellStyle dateFormat;
    private final CellStyle timeFormat;
    private final CellStyle timestampFormat;
    private final CellStyle integerFormat;
    private final CellStyle decimalFormat;

    private final String yesName;
    private final String noName;

    public DefaultExcelFormats(final Workbook excelWorkbook, final String yesName, final String noName) {
        superHeaderFormat = initSuperHeaderFormat(excelWorkbook);
        headerFormat = initHeaderFormat(excelWorkbook);
        dateFormat = initDateFormat(excelWorkbook);
        timeFormat = initTimeFormat(excelWorkbook);
        timestampFormat = initTimestampFormat(excelWorkbook);
        integerFormat = initIntegerFormat(excelWorkbook);
        decimalFormat = initDecimalFormat(excelWorkbook);

        this.yesName = yesName;
        this.noName = noName;
    }

    protected CellStyle initSuperHeaderFormat(final Workbook excelWorkbook) {
        final Font font = excelWorkbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        final CellStyle format = excelWorkbook.createCellStyle();
        format.setFont(font);
        return format;
    }

    protected CellStyle initHeaderFormat(final Workbook excelWorkbook) {
        final Font font = excelWorkbook.createFont();
        font.setBold(true);

        final CellStyle format = excelWorkbook.createCellStyle();
        format.setFont(font);
        return format;
    }

    protected CellStyle initDateFormat(final Workbook excelWorkbook) {
        final CellStyle format = excelWorkbook.createCellStyle();
        format.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("m/d/yy"));
        return format;
    }

    protected CellStyle initTimeFormat(final Workbook excelWorkbook) {
        final CellStyle format = excelWorkbook.createCellStyle();
        format.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("h:mm:ss"));
        return format;
    }

    protected CellStyle initTimestampFormat(final Workbook excelWorkbook) {
        final CellStyle format = excelWorkbook.createCellStyle();
        format.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("m/d/yy h:mm"));
        return format;
    }

    protected CellStyle initIntegerFormat(final Workbook excelWorkbook) {
        final CellStyle format = excelWorkbook.createCellStyle();
        format.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("0"));
        return format;
    }

    protected CellStyle initDecimalFormat(final Workbook excelWorkbook) {
        final CellStyle format = excelWorkbook.createCellStyle();
        format.setDataFormat(excelWorkbook.getCreationHelper().createDataFormat().getFormat("0.00"));
        return format;
    }

    @Override
    public int addSuperTitleCell(
            final Sheet sheet,
            final Row superTitleRow,
            final String text,
            final int startingCellNumber,
            final int cellCount)
    {
        if (text == null)
            throw new NullPointerException("text cannot be null");
        if (startingCellNumber < 0)
            throw new IllegalArgumentException("starting cell must be 0 or positive");
        if (cellCount <= 0)
            throw new IllegalArgumentException("cell count must be 1 or more");

        superTitleRow.createCell(startingCellNumber).setCellValue(text);
        superTitleRow.getCell(startingCellNumber).setCellStyle(superHeaderFormat);
        int nextCell = startingCellNumber + cellCount;
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startingCellNumber, nextCell - 1));

        return nextCell;
    }

    @Override
    public void addTitleCell(final Row titleRow, final String text, final int cellNumber) {
        titleRow.createCell(cellNumber).setCellValue(text);
        titleRow.getCell(cellNumber).setCellStyle(headerFormat);
    }

    @Override
    public void addIdCell(final Row row, final DbBeanInterface bean, final int cellNumber) {
        row.createCell(cellNumber).setCellValue(bean.getId());
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final String data) {
        row.createCell(cellNumber).setCellValue(data);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final boolean data) {
        row.createCell(cellNumber).setCellValue(getYesOrNoRepresentation(data));
    }

    protected String getYesOrNoRepresentation(final boolean value) {
        if (value)
            return yesName;

        return noName;
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final int data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final long data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(integerFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final Date data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(dateFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final Time data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(timeFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final Timestamp data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(timestampFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final Money data) {
        row.createCell(cellNumber).setCellValue(data.getDoubleVal());
        row.getCell(cellNumber).setCellStyle(decimalFormat);
    }

    @Override
    public void addDataCell(final Row row, final int cellNumber, final double data) {
        row.createCell(cellNumber).setCellValue(data);
        row.getCell(cellNumber).setCellStyle(decimalFormat);
    }

    @Override
    public void autosizeColumns(final Sheet sheet, final int columns) {
        for (int i = 0; i < columns; ++i)
            sheet.autoSizeColumn(i);
    }
}
