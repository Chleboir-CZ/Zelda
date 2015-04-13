/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda.notiles;

/**
 *
 * @author chleboir
 */
public class GeometryUtils {
	public static Point intersectPoint(Line line, Line line2) {
		double denominator = (line2.a*line.b - line.a*line2.b);
		if(denominator == 0)
			return null;
		return new Point((line2.b*line.c - line2.c * line.b) / denominator, -(line2.a*line.c - line.a * line2.c) / denominator);
	}	
}
