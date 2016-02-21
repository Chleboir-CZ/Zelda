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
public class IntersectInfo extends PointAndDistanceAndLine {
	public Point touchPoint;
	/**
	 * 
	 * @param dist
	 * @param p
	 * @param line 
	 */
	public IntersectInfo(double dist, Point p, Line line) {
		super(dist, p, line);
	}
}
