package org.beanmaker;

import org.dbbeans.util.Strings;

public class HTMLViewPropertyFile extends PropertyCode {

	public HTMLViewPropertyFile(final String beanName, final String packageName, final Columns columns) {
        super(beanName, packageName, "-HTML", columns);

		createSourceCode();
	}
	
	private void addFieldNamePleaseSelect(StringBuilder buf) {
        boolean hasSelects = false;
		for (Column column: columns.getList()) {
			if (column.hasAssociatedBean()) {
                hasSelects = true;
				final String fieldName = Strings.uncapitalize(SourceFiles.chopId(column.getJavaName()));
				buf.append(fieldName);
				buf.append("_please_select = ");
				buf.append(fieldName);
				buf.append("_please_select\n");
			}
		}
        if (hasSelects)
		    buf.append("\n");
	}
	
	private void addCaptchaFields(StringBuilder buf) {
		buf.append("captchaFieldName = captchaFieldName\n");
		buf.append("invalid_captcha = invalid_captcha\n\n");
	}
	
	private void addFormButtons(final StringBuilder buf) {
		buf.append("submit_button = submit_button\n");
		buf.append("reset_button = reset_button\n\n");
	}
	
	private void createSourceCode() {
		final StringBuilder buf = new StringBuilder();
		
		addComments(buf);
		addFieldNamePleaseSelect(buf);
		addCaptchaFields(buf);
		addFormButtons(buf);

		sourceCode = buf.toString();
	}
}

