package org.beanmaker.util;

import java.util.List;

public class ErrorMessage {

	public ErrorMessage(final long beanId, final String fieldName, final String fieldLabel, final String message) {
		this.beanId = beanId;
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		this.message = message;
	}
	
	public long getBeanId() {
		return beanId;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public String getFieldLabel() {
		return fieldLabel;
	}
	
	public String getMessage() {
		return message;
	}

    public String toJson() {
        final StringBuilder buf = new StringBuilder();

        buf.append("{ \"idBean\": ").append(beanId).append(", ");
        buf.append("\"fieldName\": \"").append(fieldName).append("\", ");
        buf.append("\"fieldLabel\": \"").append(fieldLabel).append("\", ");
        buf.append("\"message\": \"").append(message).append("\" }");

        return buf.toString();
    }

    public static String toJson(final List<ErrorMessage> errorMessages) {
        final StringBuilder buf = new StringBuilder();

        buf.append("\"errors\": [ ");

        for (ErrorMessage errorMessage: errorMessages)
            buf.append(errorMessage.toJson()).append(", ");

        buf.delete(buf.length() - 2, buf.length());
        buf.append(" ]");

        return buf.toString();
    }
	
	private final long beanId;
	private final String fieldName;
	private final String fieldLabel;
	private final String message;
}
