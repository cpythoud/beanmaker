package org.beanmaker.util;

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
	
	private final long beanId;
	private final String fieldName;
	private final String fieldLabel;
	private final String message;
}
