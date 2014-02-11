package org.beanmaker.util;

import java.util.List;

public class HtmlTableHelper {
	
	public static class Row {
		
		public Row(final String fieldName, final String label, final String value) {
			this.fieldName = fieldName;
			this.label = label;
			this.value = value;
		}
		
		public void print(final StringBuilder buf, final boolean odd) {
			buf.append("<tr class=\"");
			buf.append(fieldName);
			if (odd)
				buf.append(" odd");
			else
				buf.append(" even");
			buf.append("\"><th class=\"");
			buf.append(fieldName);
			buf.append("\">");
			buf.append(label);
			buf.append("</th><td class=\"");
			buf.append(fieldName);
			buf.append("\">");
			buf.append(value);
			buf.append("</td></tr>\n");
		}
		
		private final String fieldName;
		private final String label;
		private final String value;
	}
	
	public static void table(final StringBuilder buf, final String beanName, final long id, final List<Row> rows) {
		buf.append("<table id=\"");
		buf.append(beanName);
		buf.append("_");
		buf.append(id);
		buf.append("\" class=\"");
		buf.append(beanName);
		buf.append("\">\n");
		
		int index = 0;
		for (Row row: rows)
			row.print(buf, ++index % 2 != 0);
		
		buf.append("</table>\n");
	}
	
}

