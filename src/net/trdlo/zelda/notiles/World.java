/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda.notiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.trdlo.zelda.ZWorld;

/**
 *
 * @author chleboir
 */
public class World extends ZWorld {

	Collection<Line> lines;
	Collection<Line> collidableLines;
	List<IndependentPoint> independentPoints;

	Line ray;

	public World() {
		lines = new ArrayList<>();
		collidableLines = new ArrayList<>();
		independentPoints = new ArrayList<>();
//		lines.add(new Line(new Point(789, 150), new Point(900, 300)));
		Point.setLinesCollection(lines);

		independentPoints.add(new IndependentPoint(200, 200));
		independentPoints.add(new IndependentPoint(200, 390));
		independentPoints.add(new IndependentPoint(407, 400));
		independentPoints.get(0).lineTo(independentPoints.get(1)).lineTo(independentPoints.get(2));
		ray = new Line(new Point(500, 400), new Point(180, 100));
		//lines.addAll(ray.rayTraceEffect(lines));
//		Line ray = new Line(X, new Point(300, 300));
//		Line mirror = new Line(new Point(200, 400), new Point(600, 320));
//		Line reflectedRay = ray.mirrorReflection(mirror);
//		lines.add(ray);
//		lines.add(mirror);
//		lines.add(reflectedRay);

	}

	@Override
	public void update() {

	}

	public IndependentPoint getPointAt(int x, int y) {
		for(IndependentPoint p : this.independentPoints) {
			if (Math.abs(p.x - x) < 6 && Math.abs(p.y - y) < 6) {
				return p;
			}
		}
		return null;
	}
	
	public Collection<IndependentPoint> pointsInRect(Point A, Point B) {
		Collection<IndependentPoint> pointsInRect = new ArrayList<>();
		
		Rectangle rect = new Rectangle(A.getJavaPoint());
		rect.add(B.getJavaPoint());
		
		
		for(IndependentPoint p : independentPoints) {
			//if(p.x > A.x && p.x < B.x && p.y > A.y && p.y < B.y) {
			if (rect.contains(p.getJavaPoint())) {
				pointsInRect.add(p);
			}
		}
		return pointsInRect;
	}
	
	public void delSelectedPoints() {
		boolean deleted;
		do {
			deleted = false;
			for(IndependentPoint iP : independentPoints) {
				if(iP.isSelected()) {
					independentPoints.remove(iP);
					deleted = true;
					break;
				}
			}
		} while (deleted);
	}
	
	public void removePoint(IndependentPoint iP) {
		independentPoints.remove(iP);
	}
}
