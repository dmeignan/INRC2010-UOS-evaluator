/**
 * Copyright 2012-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.util;


import javax.swing.ImageIcon;

/**
 * Static methods for managing icons.
 * 
 * @author David Meignan
 */
public class IconUtils {
	
	private static IconUtils resourceRef;
	
	/**
	 * Returns an icon from its relative path or name. Returns <code>null</code> 
	 * if no image is found.
	 * 
	 * @param path the relative path or the name of the image.
	 * @param description the description of the image.
	 * @return an icon from its relative path or name. Returns <code>null</code> 
	 * if no image is found.
	 */
	public static ImageIcon createImageIcon(String path, 
			String description) {
		if (resourceRef == null) {
			resourceRef = new IconUtils();
		}
		java.net.URL imgURL = resourceRef.getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	/**
	 * Returns the URL of an image from its path or name. Returns
	 * <code>null</code> if the image is not found.
	 * 
	 * @param path the relative path or the name of the image.
	 * @return the URL of an image from its path or name.
	 */
	public static String getImageURL(String path) {
		if (resourceRef == null) {
			resourceRef = new IconUtils();
		}
		java.net.URL imgURL = resourceRef.getClass().getResource(path);
		if (imgURL != null) {
			return imgURL.toString();
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
}
