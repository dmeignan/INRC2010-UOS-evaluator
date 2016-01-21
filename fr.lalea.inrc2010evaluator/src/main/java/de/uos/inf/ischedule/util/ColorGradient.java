/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Color gradient.
 * 
 * @author David Meignan
 */
public class ColorGradient {

	private ArrayList<Color> stopColors;
	
	/**
	 * Constructs a color gradient from a list of stops.
	 * The list must not be null or empty.
	 * 
	 * @param stops the list of stops.
	 */
	public ColorGradient(List<Color> stops) {
		if (stops == null || stops.isEmpty())
			throw new IllegalArgumentException("The gradient must contain at least " +
					"one color.");
		stopColors = new ArrayList<Color>(stops);
		if (stopColors.size() == 1)
			stopColors.add(stopColors.get(0));
	}
	
	/**
	 * Returns the color at a given position of the gradient.
	 * The position should be between the values 0 and 1.
	 * 
	 * @param position the position.
	 * @return the color at a given position of the gradient.
	 */
	public Color getColorAt(double position) {
		if (position >= 1.)
			return stopColors.get(stopColors.size()-1);
		if (position <= 0.)
			return stopColors.get(0);
		
		int gradientCount = stopColors.size()-1;
		int gradientIdx = 
				(int) ( ((double)position) * ((double)gradientCount) );
		double startGradient = 
				((double)gradientIdx) / ((double)gradientCount);
		double posInGradient = (position - startGradient) / ((double)gradientCount);
		
		Color startColor = stopColors.get(gradientIdx);
		Color endColor = stopColors.get(gradientIdx+1);
		if (posInGradient >= 1.)
			return endColor;
		if (posInGradient <= 0.)
			return startColor;
		
		int startR = startColor.getRed();
		int startG = startColor.getGreen();
		int startB = startColor.getBlue();
		int endR = endColor.getRed();
		int endG = endColor.getGreen();
		int endB = endColor.getBlue();
		
		int colorR = (int) ((((double)(endR-startR))*posInGradient)+startR);
		int colorG = (int) ((((double)(endG-startG))*posInGradient)+startG);
		int colorB = (int) ((((double)(endB-startB))*posInGradient)+startB);
		colorR = Math.max(0, colorR);
		colorR = Math.min(255, colorR);
		colorG = Math.max(0, colorG);
		colorG = Math.min(255, colorG);
		colorB = Math.max(0, colorB);
		colorB = Math.min(255, colorB);
		
		return new Color(colorR, colorG, colorB);
	}
	
}
