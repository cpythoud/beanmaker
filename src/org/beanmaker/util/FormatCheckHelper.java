package org.beanmaker.util;

import java.util.regex.Pattern;

import org.dbbeans.util.EmailValidator;
import org.dbbeans.util.SimpleInputDateFormat;

public class FormatCheckHelper {
	
	public static boolean isNumber(final String s) {
		return NUMBER_PATTERN.matcher(s).matches();
	}
	
	public static boolean isValidIsoDate(final String s) {
		return ISO_DATE_FORMAT.validate(s);
	}
	
	public static boolean isEmailValid(final String s) {
		return EmailValidator.validate(s, true, true);
	}
	
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
	private static final SimpleInputDateFormat ISO_DATE_FORMAT = new SimpleInputDateFormat(SimpleInputDateFormat.ElementOrder.YYMD, "-");
}

