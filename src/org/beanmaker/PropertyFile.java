package org.beanmaker;

public class PropertyFile extends PropertyCode {
	
	public PropertyFile(final String beanName, final String packageName, final Columns columns) {
        super(beanName, packageName, "", columns);

		createSourceCode();
	}
	
	private void addFieldNames(StringBuilder buf) {
		for (Column column: columns.getList()) {
			buf.append(column.getJavaName());
			buf.append(" = ");
			buf.append(column.getJavaName());
			buf.append("\n");
		}
		buf.append("\n");
	}
	
	private void addFieldRequired(StringBuilder buf) {
		for (Column column: columns.getList()) {
			if (!column.isSpecial()) {
				buf.append(column.getJavaName());
				buf.append("_required = ");
				buf.append(column.getJavaName());
				buf.append("_required");
				buf.append("\n");
			}
		}
		buf.append("\n");
	}
	
	private void addFieldFormatError(StringBuilder buf) {
		for (Column column: columns.getList()) {
			if (!column.isSpecial()) {
				buf.append(column.getJavaName());
				buf.append("_bad_format = ");
				buf.append(column.getJavaName());
				buf.append("_bad_format");
				buf.append("\n");
			}
		}
		buf.append("\n");
	}

    private void addFieldNotUniqueError(StringBuilder buf) {
        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                buf.append(column.getJavaName());
                buf.append("_not_unique = ");
                buf.append(column.getJavaName());
                buf.append("_not_unique");
                buf.append("\n");
            }
        }
        buf.append("\n");
    }
	
	private void addBooleanValues(final StringBuilder buf) {
		buf.append("true_value = true_value\n");
		buf.append("false_value = false_value\n\n");
	}
	
	private void createSourceCode() {
		final StringBuilder buf = new StringBuilder();
		
		addComments(buf);
		addFieldNames(buf);
		addFieldRequired(buf);
		addFieldFormatError(buf);
        addFieldNotUniqueError(buf);
		addBooleanValues(buf);
		
		sourceCode = buf.toString();
	}
}

