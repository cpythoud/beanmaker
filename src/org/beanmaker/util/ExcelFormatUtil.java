package org.beanmaker.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import org.dbbeans.util.Pair;

public class ExcelFormatUtil {

    public static Pair<Cell, Integer> addMultiColumnCell(
            final Sheet sheet,
            final Row row,
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

        row.createCell(startingCellNumber).setCellValue(text);
        int nextCell = startingCellNumber + cellCount;
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startingCellNumber, nextCell - 1));

        return new Pair<Cell, Integer>(row.getCell(startingCellNumber), nextCell);
    }
}
