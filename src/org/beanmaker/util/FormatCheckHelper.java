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

	public static boolean isIntNumber(final String s) {
		try {
			Integer.parseInt(s);
		} catch (final NumberFormatException nfex) {
			return false;
		}

		return true;
	}

	public static boolean isLongNumber(final String s) {
		try {
			Long.parseLong(s);
		} catch (final NumberFormatException nfex) {
			return false;
		}

		return true;
	}
}

