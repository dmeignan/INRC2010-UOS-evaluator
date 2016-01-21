/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Class for local-specific string.
 * 
 * @author David Meignan
 */
public class Messages {
	private static final String BUNDLE_NAME = "de.uos.inf.ischedule.util.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	
	private static DateTimeFormatter shortDateFormatter;

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static String getShortDateString(LocalDate date) {
		if (shortDateFormatter == null)
			shortDateFormatter = ISODateTimeFormat.date();
		return shortDateFormatter.print(date);
	}
}
