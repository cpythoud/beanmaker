package org.beanmaker.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import org.dbbeans.util.Money;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public interface ExcelFormats {

    int addSuperTitleCell(
            final Sheet sheet,
            final Row superTitleRow,
            final String text,
            final int startingCellNumber,
            final int cellCount);

    void addTitleCell(final Row titleRow, final String text, final int cellNumber);

    void addIdCell(final Row row, final DbBeanInterface bean, final int cellNumber);

    void addDataCell(final Row row, final int cellNumber, final String data);

    void addDataCell(final Row row, final int cellNumber, final boolean data);

    void addDataCell(final Row row, final int cellNumber, final int data);

    void addDataCell(final Row row, final int cellNumber, final long data);

    void addDataCell(final Row row, final int cellNumber, final Date data);

    void addDataCell(final Row row, final int cellNumber, final Time data);

    void addDataCell(final Row row, final int cellNumber, final Timestamp data);

    void addDataCell(final Row row, final int cellNumber, final Money data);

    void addDataCell(final Row row, final int cellNumber, final double data);

    void autosizeColumns(final Sheet sheet, final int columns);
}
